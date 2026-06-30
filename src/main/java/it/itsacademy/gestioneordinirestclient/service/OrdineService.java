package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.UUID;

public interface OrdineService {
    OrdineDTO creaOrdine(CreaOrdineDTO nuovoOrdine, String username);

    OrdineDTO pagaOrdine(UUID idOrdine);

    void pagaOrdine(UUID idOrdine, MultipartFile file);

    OrdineDTO cercaOrdine(UUID idOrdine);

    Collection<OrdineDTO> cercaTutti();

    Collection<PagamentoDTO> pagamentiDellOrdine(UUID idOrdine);

    void cancellaOrdine(UUID idOrdine);
}
