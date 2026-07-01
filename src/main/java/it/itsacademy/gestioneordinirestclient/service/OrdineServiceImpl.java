package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.exception.*;
import it.itsacademy.gestioneordinirestclient.exception.dto.GeneralErrorResponseDTO;
import it.itsacademy.gestioneordinirestclient.mapper.OrdineMapper;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor @Slf4j
public class OrdineServiceImpl implements OrdineService {
    private final OrdineMapper mapper;
    private final RepositoryOrdine repositoryOrdine;
    private final RestClient restClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // importante per estrarre il messaggio d'errore 402 dall'altro microservizio
    private final S3AsyncClient s3;

    @Value("${features.s3.receipt-upload-enabled}")
    private boolean isS3UploadEnabled;

    @Value("${s3.bucket.name}")
    private String bucketS3;

    @Value("${s3.bucket.prefixes.root}")
    private String rootPrefix;

    @Value("${api.gestione-pagamenti.url}")
    private String gestionePagamentiUrl;

    @Value("${api.auth.url}")
    private String authUrl;

    private Ordine validateOrderForPayment(UUID idOrdine) {
        log.debug("Paying order. id={}", idOrdine);
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        // Controlla che l'ordine non sia già stato pagato o in elaborazione (per evitare di inondare di richieste inutili)
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.PAGATO || ordine.getStatoOrdine() == Ordine.StatoOrdine.IN_ELABORAZIONE || ordine.getStatoOrdine() == Ordine.StatoOrdine.IN_ELABORAZIONE_CON_FILE) {
            log.debug("Tried to pay order that was already paid or elaborating. id={} orderStatus={}", ordine.getIdOrdine(), ordine.getStatoOrdine());
            throw new ConflictException("Non è possibile pagare un ordine già pagato o in elaborazione");
        }

        // Controlla che l'ordine non sia cancellato
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.ELIMINATO) {
            log.debug("Tried to pay deleted order. id={}", ordine.getIdOrdine());
            throw new ConflictException("Non è possibile pagare un ordine eliminato");
        }

        return ordine;
    }

    @Override
    public OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine, String username) {
        log.debug("Creating order, description={}", nuovoOrdine.getDescrizione());
        UtenteDTO utente;
        try {
            String uri = authUrl + "/" + username;
            log.debug("Calling auth service. uri={}", uri);
            utente = restClient.get()
                    .uri(authUrl + "/{username}", username)
                    .retrieve()
                    .body(UtenteDTO.class);
            log.trace("Retrieved user username={}, email={}",
                    utente.getUsername(),
                    utente.getEmail());
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody == null || responseBody.isBlank())
                throw new RuntimeException("Comunication error with Auth service: " + e.getMessage());

            GeneralErrorResponseDTO errorResponse = objectMapper.readValue(
                    responseBody,
                    GeneralErrorResponseDTO.class
            );
            log.warn("The uri threw an exception. status={}, message={}", errorResponse.getStatus(), errorResponse.getMessage());
            if (errorResponse.getStatus() == 402) throw new PaymentRequiredException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 404) throw new NotFoundException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 409) throw new ConflictException(errorResponse.getMessage());
            else throw new RuntimeException(errorResponse.getMessage());
        }
        Ordine daCreare = mapper.toEntity(nuovoOrdine);
        daCreare.setUsernameCliente(username);
        daCreare.setEmailCliente(utente.getEmail());

        Ordine salvato = repositoryOrdine.save(daCreare);

        log.debug("Created order. id={}", salvato.getIdOrdine());
        return mapper.toDTO(salvato);
    }

    @Override
    public OrdineDTO pagaOrdine(UUID idOrdine) {
        Ordine ordine = validateOrderForPayment(idOrdine);

        // Prima mettiamo l'ordine come pagato poi inviamo il messaggio sulla coda.
        // Se facessimo il contrario potrebbe accadere che il .save vada in errore ma il messaggio è già stato inviato
        // al microservizi dei pagamenti, quindi accadrebbe che il pagamento non risulta pagato anche se il pagamento
        // è avvenuto con successo: il messaggio è inviato all'altro microservizio, il .save fallisce, rollback su
        // questo metodo (ma non sull'altro microservizio), pagamento non .PAGATO ma pagamento riuscito nell'altro microservizio.
        // In questo modo se il .save fallisce la transazione viene rollbackata prima ancora di inviare il messaggio.
        // Per buona norma l'invio del messaggio andrebbe sempre all'ultimo.
        log.trace("Order id={} has been marked as \"IN_ELABORAZIONE\"", ordine.getIdOrdine());
        ordine.setStatoOrdine(Ordine.StatoOrdine.IN_ELABORAZIONE); // Segna il pagamento come in elaborazione
        Ordine salvato = repositoryOrdine.save(ordine);

        // Invia il messaggio all'exchange "payments.exchange" con routing key "payments.order.created". Ci penserà
        // lui a inviarla sulla queue corretta attraverso il binding.
        rabbitTemplate.convertAndSend("payments.exchange", "payments.order.created",
                new CreaPagamentoDTO(ordine.getIdOrdine(), ordine.getTotale()));

        return mapper.toDTO(salvato);
    }

    @Override
    public void pagaOrdine(UUID idOrdine, MultipartFile file) {
        Ordine ordine = validateOrderForPayment(idOrdine);
        final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File size must not exceed 5 MB");
        if (!"application/pdf".equals(file.getContentType())) throw new IllegalArgumentException("Uploaded file must be a PDF.");
        if (isS3UploadEnabled) {
            log.debug("S3 upload is enabled. Proceeding with upload...");

            final String s3ObjectKey = rootPrefix + "/" + ordine.getUsernameCliente() + "/" + file.getOriginalFilename() + ".pdf"; // Che nome dare all'oggetto s3
            ordine.setStatoOrdine(Ordine.StatoOrdine.IN_ELABORAZIONE);

            try {
                // Leggi i bytes direttamente dal Multipart file
                byte[] fileBytes = file.getBytes();

                s3.putObject(b -> b.bucket(bucketS3).key(s3ObjectKey).contentType("application/pdf").build(),
                                AsyncRequestBody.fromBytes(fileBytes))
                        // Gestisce l'asincronia come una sincronia: così rimane nella stessa transazione.
                        // Non usiamo whenCompleted perchè whenCompleted viene eseguito da un altro thread e uscirebbe dalla transazione.
                        // Alla fine del metodo l'ordine verrebbe comunque cambiato (per il dirty checking) ma nel frattempo
                        // la chiamata asincrona si troverebbe con un ordine al vecchio stato (da pagare) e fallirebbe.
                        .join();
                log.info("File uploaded correctly.");
            } catch (IOException e) {
                log.error("Failed to read bytes from uploaded file", e);
                throw new RuntimeException("Failed to read bytes from uploaded file", e);
            } catch (Exception e) {
                log.error("Error during the upload of {}: {}", s3ObjectKey, e.getMessage());
                throw new RuntimeException("Error uploading file to S3", e);
            }

            ordine.setStatoOrdine(Ordine.StatoOrdine.IN_ELABORAZIONE_CON_FILE);
            repositoryOrdine.saveAndFlush(ordine);

            try {
                rabbitTemplate.convertAndSend("payments.exchange", "payments.order.created",
                    new CreaPagamentoDTO(ordine.getIdOrdine(), ordine.getTotale()));
            } catch (Exception e) {
                log.error("Failed to send payment request to RabbitMQ for order ID: {}", ordine.getIdOrdine(), e);
                throw new RuntimeException("Error processing file upload", e);
            }
        } else {
            log.warn("S3 upload has been disabled. Rolling back the payment for the order. idOrdine={}", idOrdine);
            ordine.setStatoOrdine(Ordine.StatoOrdine.DA_PAGARE);
        }
    }

    @Override
    public OrdineDTO cercaOrdine(UUID idOrdine) {
        log.debug("Fetching order. id={}", idOrdine);
        return mapper.toDTO(findByIdOrLogAndThrow(idOrdine));
    }

    @Override
    public Collection<OrdineDTO> cercaTutti() {
        log.debug("Listing orders");
        return mapper.toDTO(repositoryOrdine.findAllNotDeleted());
    }

    @Override
    public Collection<PagamentoDTO> pagamentiDellOrdine(UUID idOrdine) {
        log.debug("Searching payments of order with id={}", idOrdine);
        findByIdOrLogAndThrow(idOrdine);

        try {
            String uri = gestionePagamentiUrl + "/pagamenti/" + idOrdine;
            log.debug("Calling payments service. uri={}", uri);
            return restClient.get()
                .uri(gestionePagamentiUrl + "/pagamenti/{idOrdine}", idOrdine)
                .retrieve()
                .body(new ParameterizedTypeReference<Collection<PagamentoDTO>>() {});
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody == null || responseBody.isBlank())
                throw new RuntimeException("Comunication error with Auth service: " + e.getMessage());
            GeneralErrorResponseDTO errorResponse =
                    // Questo metodo serve a trasformare il json di ritorno in una classe java
                    objectMapper.readValue(
                            responseBody, // Questo contiene una stringa che contiene tutto il json
                            GeneralErrorResponseDTO.class // Questo dice all'API di convertire quel json nella nostra classe
                    );
            log.warn("The uri threw an exception. status={}, message={}", errorResponse.getStatus(), errorResponse.getMessage());
            if (errorResponse.getStatus() == 402) throw new PaymentRequiredException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 404) throw new NotFoundException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 409) throw new ConflictException(errorResponse.getMessage());
            else throw new RuntimeException(errorResponse.getMessage());
        }
    }

    @Override
    public void cancellaOrdine(UUID idOrdine) {
        log.debug("Deleting order with id={}", idOrdine);
        Ordine trovato = findByIdOrLogAndThrow(idOrdine);

        if (trovato.getStatoOrdine() == Ordine.StatoOrdine.PAGATO || trovato.getStatoOrdine() == Ordine.StatoOrdine.IN_ELABORAZIONE) {
            log.warn("Tried to delete order that was already paid or elaborating. id={} orderStatus={}", trovato.getIdOrdine(), trovato.getStatoOrdine());
            throw new ConflictException("Non è possibile cancellare un ordine già pagato o in elaborazione.");
        }

        trovato.setStatoOrdine(Ordine.StatoOrdine.ELIMINATO);
    }

    private Ordine findByIdOrLogAndThrow(UUID idOrdine) {
        try {
            return repositoryOrdine.findByIdOrThrow(idOrdine);
        } catch (NotFoundException e) {
            log.debug("Order not found. id={}", idOrdine);
            throw e;
        }
    }
}
