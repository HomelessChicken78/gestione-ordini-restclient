package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.mapper.OrdineMapper;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class OrdineServiceImpl implements OrdineService {
    private final OrdineMapper mapper;
    private final RepositoryOrdine repositoryOrdine;

    @Override
    public OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine) {
        Ordine daCreare = mapper.toEntity(nuovoOrdine);

        Ordine salvato = repositoryOrdine.save(daCreare);

        return mapper.toDTO(salvato);
    }
}
