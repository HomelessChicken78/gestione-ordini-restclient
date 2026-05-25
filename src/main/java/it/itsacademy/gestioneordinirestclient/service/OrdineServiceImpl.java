package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.mapper.OrdineMapper;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {
    private final OrdineMapper mapper;
    private final RepositoryOrdine repositoryOrdine;
    private final RestClient restClient;

    @Override
    public OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine) {
        Ordine daCreare = mapper.toEntity(nuovoOrdine);

        Ordine salvato = repositoryOrdine.save(daCreare);

        return mapper.toDTO(salvato);
    }

    @Override
    public OrdineDTO pagaOrdine(UUID idOrdine) {
        Ordine ordine = repositoryOrdine.findByIdOrThrow(idOrdine);

        PagamentoDTO risposta = restClient.post()
                .uri("http://localhost:8081/api/pagamenti")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PagamentoDTO.class);
        // TODO controllare che non dia un 402

        ordine.setStatoOrdine(Ordine.StatoOrdine.PAGATO);
        Ordine salvato = repositoryOrdine.save(ordine);

        return mapper.toDTO(salvato);
    }
}
