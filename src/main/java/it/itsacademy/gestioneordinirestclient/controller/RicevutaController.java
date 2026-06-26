package it.itsacademy.gestioneordinirestclient.controller;

import it.itsacademy.gestioneordinirestclient.dto.PresignedUrlDTO;
import it.itsacademy.gestioneordinirestclient.service.RicevutaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/receipts")
@RequiredArgsConstructor @Slf4j
public class RicevutaController {
    private final RicevutaService ricevutaService;
    private static final String json = "application/json";

    @GetMapping(path = "/{receiptFileName}", produces = json)
    public PresignedUrlDTO downloadReceipt(@RequestHeader("X-Authenticated-User") String authHeader,
                                           @PathVariable String receiptFileName)

    {
        PresignedUrlDTO response = ricevutaService.createPresignedUrl(authHeader, receiptFileName);
        log.info("Created presigned url for download. fileName={}, presigned url={}",
                receiptFileName, response.getPresignedUrl());
        return response;
    }

    @GetMapping(produces = json)
    public List<String> searchAllReceipts(@RequestHeader("X-Authenticated-User") String authHeader) {
        List<String> response = ricevutaService.searchAllReceiptsOfUser(authHeader);
        log.info("Search request for all receipt. username={}", authHeader);
        return response;
    }
}
