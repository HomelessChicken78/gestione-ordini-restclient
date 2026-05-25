package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;

import java.util.UUID;

public interface OrdineService {
    OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine);

    OrdineDTO pagaOrdine(UUID idOrdine);
}
