package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;

import java.util.Collection;
import java.util.UUID;

public interface OrdineService {
    OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine);

    OrdineDTO pagaOrdine(UUID idOrdine);

    OrdineDTO cercaOrdine(UUID idOrdine);

    Collection<OrdineDTO> cercaTutti();

    Collection<PagamentoDTO> pagamentiDellOrdine(UUID idOrdine);

    void cancellaOrdine(UUID idOrdine);
}
