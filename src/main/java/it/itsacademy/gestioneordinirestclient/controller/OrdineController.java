package it.itsacademy.gestioneordinirestclient.controller;

import it.itsacademy.gestioneordinirestclient.dto.CreaOrdineDTO;
import it.itsacademy.gestioneordinirestclient.dto.OrdineDTO;
import it.itsacademy.gestioneordinirestclient.dto.PagamentoDTO;
import it.itsacademy.gestioneordinirestclient.service.OrdineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/ordini")
@RequiredArgsConstructor
public class OrdineController {
    private final OrdineService ordineService;
    private static final String json = "application/json";

    @PostMapping(consumes = json, produces = json)
    @ResponseStatus(HttpStatus.CREATED)
    public OrdineDTO creaOrdine(@RequestBody CreaOrdineDTO nuovoOrdine) {
        return ordineService.creaOrdine(nuovoOrdine);
    }

    @PatchMapping(path = "/{idOrdine}/paga", produces = json)
    public OrdineDTO pagaOrdine(@PathVariable UUID idOrdine) {
        return ordineService.pagaOrdine(idOrdine);
    }

    @GetMapping(path = "/{idOrdine}", produces = json)
    public OrdineDTO cercaOrdine(@PathVariable UUID idOrdine) {
        return ordineService.cercaOrdine(idOrdine);
    }

    @GetMapping(produces = json)
    public Collection<OrdineDTO> cercaTutti() {
        return ordineService.cercaTutti();
    }

    @DeleteMapping(path = "/{idOrdine}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancellaOrdine(@PathVariable UUID idOrdine) {
        ordineService.cancellaOrdine(idOrdine);
    }

    @GetMapping(path = "/{idOrdine}/pagamenti", produces = json)
    public Collection<PagamentoDTO> pagamentiDellOrdine(@PathVariable UUID idOrdine) {
        return ordineService.pagamentiDellOrdine(idOrdine);
    }
}