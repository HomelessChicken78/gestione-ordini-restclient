package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.exception.*;
import it.itsacademy.gestioneordinirestclient.exception.dto.GeneralErrorResponseDTO;
import it.itsacademy.gestioneordinirestclient.mapper.OrdineMapper;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {
    private final OrdineMapper mapper;
    private final RepositoryOrdine repositoryOrdine;
    private final RestClient restClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // importante per estrarre il messaggio d'errore 402 dall'altro microservizio

    @Value("${api.gestione-pagamenti.url}")
    private String gestionePagamentiUrl;

    @Value("${api.auth.url}")
    private String authUrl;

    @Override
    public OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine, String username) {
        UtenteDTO utente;
        try {
            utente = restClient.get()
                    .uri(authUrl + "/" + username)
                    .retrieve()
                    .body(UtenteDTO.class);
        } catch (HttpClientErrorException e) {
            GeneralErrorResponseDTO errorResponse = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    GeneralErrorResponseDTO.class
            );
            if (errorResponse.getStatus() == 402) throw new PaymentRequiredException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 404) throw new NotFoundException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 409) throw new ConflictException(errorResponse.getMessage());
            else throw new RuntimeException(errorResponse.getMessage());
        }
        Ordine daCreare = mapper.toEntity(nuovoOrdine);
        daCreare.setUsernameCliente(username);
        daCreare.setEmailCliente(utente.getEmail());

        Ordine salvato = repositoryOrdine.save(daCreare);

        return mapper.toDTO(salvato);
    }

    @Override
    public OrdineDTO pagaOrdine(UUID idOrdine) {
        Ordine ordine = repositoryOrdine.findByIdOrThrow(idOrdine);

        // Controlla che l'ordine non sia già stato pagato o in elaborazione (per evitare di inondare di richieste inutili)
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.PAGATO || ordine.getStatoOrdine() == Ordine.StatoOrdine.IN_ELABORAZIONE)
            throw new ConflictException("Non è possibile pagare un ordine già pagato o in elaborazione");

        // Controlla che l'ordine non sia cancellato
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.ELIMINATO)
            throw new ConflictException("Non è possibile pagare un ordine eliminato");

        // Prima mettiamo l'ordine come pagato poi inviamo il messaggio sulla coda.
        // Se facessimo il contrario potrebbe accadere che il .save vada in errore ma il messaggio è già stato inviato
        // al microservizi dei pagamenti, quindi accadrebbe che il pagamento non risulta pagato anche se il pagamento
        // è avvenuto con successo: il messaggio è inviato all'altro microservizio, il .save fallisce, rollback su
        // questo metodo (ma non sull'altro microservizio), pagamento non .PAGATO ma pagamento riuscito nell'altro microservizio.
        // In questo modo se il .save fallisce la transazione viene rollbackata prima ancora di inviare il messaggio.
        // Per buona norma l'invio del messaggio andrebbe sempre all'ultimo.
        ordine.setStatoOrdine(Ordine.StatoOrdine.IN_ELABORAZIONE); // Segna il pagamento come in elaborazione
        Ordine salvato = repositoryOrdine.save(ordine);

        // Invia il messaggio all'exchange "payments.exchange" con routing key "payments.order.created". Ci penserà
        // lui a inviarla sulla queue corretta attraverso il binding.
        rabbitTemplate.convertAndSend("payments.exchange", "payments.order.created",
                new CreaPagamentoDTO(ordine.getIdOrdine(), ordine.getTotale()));

        return mapper.toDTO(salvato);
    }

    @Override
    public OrdineDTO cercaOrdine(UUID idOrdine) {
        return mapper.toDTO(repositoryOrdine.findByIdOrThrow(idOrdine));
    }

    @Override
    public Collection<OrdineDTO> cercaTutti() {
        return mapper.toDTO(repositoryOrdine.findAllNotDeleted());
    }

    @Override
    public Collection<PagamentoDTO> pagamentiDellOrdine(UUID idOrdine) {
        repositoryOrdine.findByIdOrThrow(idOrdine);

        try {
        return restClient.get()
                .uri(gestionePagamentiUrl + "/pagamenti/" + idOrdine)
                .retrieve()
                .body(new ParameterizedTypeReference<Collection<PagamentoDTO>>() {});
        } catch (HttpClientErrorException e) {
            GeneralErrorResponseDTO errorResponse =
                    // Questo metodo serve a trasformare il json di ritorno in una classe java
                    objectMapper.readValue(
                            e.getResponseBodyAsString(), // Questo contiene una stringa che contiene tutto il json
                            GeneralErrorResponseDTO.class // Questo dice all'API di convertire quel json nella nostra classe
                    );
            if (errorResponse.getStatus() == 402) throw new PaymentRequiredException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 404) throw new NotFoundException(errorResponse.getMessage());
            else if (errorResponse.getStatus() == 409) throw new ConflictException(errorResponse.getMessage());
            else throw new RuntimeException(errorResponse.getMessage());
        }
    }

    @Override
    public void cancellaOrdine(UUID idOrdine) {
        Ordine trovato = repositoryOrdine.findByIdOrThrow(idOrdine);

        if (trovato.getStatoOrdine() == Ordine.StatoOrdine.PAGATO || trovato.getStatoOrdine() == Ordine.StatoOrdine.IN_ELABORAZIONE)
            throw new ConflictException("Non è possibile cancellare un ordine già pagato o in elaborazione.");

        trovato.setStatoOrdine(Ordine.StatoOrdine.ELIMINATO);
    }
}
