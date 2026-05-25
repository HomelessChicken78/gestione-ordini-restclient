package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.exception.ConflictException;
import it.itsacademy.gestioneordinirestclient.exception.PaymentRequiredException;
import it.itsacademy.gestioneordinirestclient.exception.dto.GeneralErrorResponseDTO;
import it.itsacademy.gestioneordinirestclient.mapper.OrdineMapper;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {
    private final OrdineMapper mapper;
    private final RepositoryOrdine repositoryOrdine;
    private final RestClient restClient;
    private final ObjectMapper objectMapper; // importante per estrarre il messaggio d'errore 402 dall'altro microservizio

    @Override
    public OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine) {
        Ordine daCreare = mapper.toEntity(nuovoOrdine);

        Ordine salvato = repositoryOrdine.save(daCreare);

        return mapper.toDTO(salvato);
    }

    @Override
    public OrdineDTO pagaOrdine(UUID idOrdine) {
        Ordine ordine = repositoryOrdine.findByIdOrThrow(idOrdine);

        // Controlla che l'ordine non sia già stato pagato
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.PAGATO)
            throw new ConflictException("Non è possibile pagare un ordine già pagato");

        // Controlla che l'ordine non sia cancellato
        if (ordine.getStatoOrdine() == Ordine.StatoOrdine.ELIMINATO)
            throw new ConflictException("Non è possibile pagare un ordine eliminato");

        try {
            PagamentoDTO risposta = restClient.post()
                    .uri("http://localhost:8081/api/pagamenti/" + idOrdine)
                    .body(new CreaPagamentoDTO(ordine.getTotale()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(PagamentoDTO.class);
        } catch (HttpClientErrorException e) {
            GeneralErrorResponseDTO errorResponse =
                    // Questo metodo serve a trasformare il json di ritorno in una classe java
                    objectMapper.readValue(
                            e.getResponseBodyAsString(), // Questo contiene una stringa che contiene tutto il json
                            GeneralErrorResponseDTO.class // Questo dice all'API di convertire quel json nella nostra classe
                    );

            // Se è un 402:
            if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED)
                throw new PaymentRequiredException(errorResponse.getMessage()); // Grazie a quello fatto prima possiamo estrarre il messaggio
            throw new RuntimeException("Unknown error.");
        }
        // TODO controllare che non dia un 402

        ordine.setStatoOrdine(Ordine.StatoOrdine.PAGATO);
        Ordine salvato = repositoryOrdine.save(ordine);

        return mapper.toDTO(salvato);
    }
}
