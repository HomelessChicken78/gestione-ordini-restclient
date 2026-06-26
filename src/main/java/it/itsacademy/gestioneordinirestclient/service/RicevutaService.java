package it.itsacademy.gestioneordinirestclient.service;

import it.itsacademy.gestioneordinirestclient.dto.PresignedUrlDTO;

import java.util.List;

public interface RicevutaService {
    List<String> searchAllReceiptsOfUser(String authHeader);

    PresignedUrlDTO createPresignedUrl(String username, String receiptFileName);
}
