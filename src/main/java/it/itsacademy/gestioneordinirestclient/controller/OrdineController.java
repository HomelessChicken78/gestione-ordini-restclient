package it.itsacademy.gestioneordinirestclient.controller;

import it.itsacademy.gestioneordinirestclient.dto.CreaOrdineDTO;
import it.itsacademy.gestioneordinirestclient.dto.OrdineDTO;
import it.itsacademy.gestioneordinirestclient.dto.PagamentoDTO;
import it.itsacademy.gestioneordinirestclient.service.OrdineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/ordini")
@RequiredArgsConstructor @Slf4j
public class OrdineController {
    // Lo fa lombok:
    //private static final Logger log =
    //        LoggerFactory.getLogger(OrderController.class);
    private final OrdineService ordineService;
    private static final String json = "application/json";

    @PostMapping(consumes = json, produces = json)
    @ResponseStatus(HttpStatus.CREATED)
    public OrdineDTO creaOrdine(@RequestBody CreaOrdineDTO nuovoOrdine, @RequestHeader("X-Authenticated-User") String authHeader) {
        // NB: Principal è auto-iniettato da spring
        OrdineDTO newOrder = ordineService.creaOrdine(nuovoOrdine, authHeader);
        log.info("Order with id={} created by user={}",
                newOrder.getIdOrdine(),
                authHeader);
        return newOrder;
    }

    @PatchMapping(path = "/{idOrdine}/paga", produces = json)
    public OrdineDTO pagaOrdine(@PathVariable UUID idOrdine, @RequestHeader("X-Authenticated-User") String authHeader) {
        OrdineDTO paidOrder = ordineService.pagaOrdine(idOrdine);
        log.info("Order with id={} paid by user={}",
                paidOrder.getIdOrdine(),
                authHeader);
        return paidOrder;
    }

    @GetMapping(path = "/{idOrdine}", produces = json)
    public OrdineDTO cercaOrdine(@PathVariable UUID idOrdine, @RequestHeader("X-Authenticated-User") String authHeader) {
        OrdineDTO searchedOrder = ordineService.cercaOrdine(idOrdine);
        log.info("Order lookup with id={} by user={}",
                searchedOrder.getIdOrdine(),
                authHeader);
        return searchedOrder;
    }

    @GetMapping(produces = json)
    public Collection<OrdineDTO> cercaTutti(@RequestHeader("X-Authenticated-User") String authHeader) {
        Collection<OrdineDTO> allOrders = ordineService.cercaTutti();
        log.info("Order list from user={}",
                authHeader);
        return allOrders;
    }

    @DeleteMapping(path = "/{idOrdine}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancellaOrdine(@PathVariable UUID idOrdine, @RequestHeader("X-Authenticated-User") String authHeader) {
        ordineService.cancellaOrdine(idOrdine);
        log.info("Order with id={} deletion from user={}",
                idOrdine,
                authHeader);
    }

    @GetMapping(path = "/{idOrdine}/pagamenti", produces = json)
    public Collection<PagamentoDTO> pagamentiDellOrdine(@PathVariable UUID idOrdine, @RequestHeader("X-Authenticated-User") String authHeader) {
        Collection<PagamentoDTO> paymentsOfOrder = ordineService.pagamentiDellOrdine(idOrdine);
        log.info("Order payments search for order with id={} by user={}",
                idOrdine,
                authHeader);
        return paymentsOfOrder;
    }

    @GetMapping(path = "/health")
    public void health() {}
}