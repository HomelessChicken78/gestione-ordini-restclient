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
        log.info("Order with id={} created",
                newOrder.getIdOrdine());
        return newOrder;
    }

    @PatchMapping(path = "/{idOrdine}/paga", produces = json)
    public OrdineDTO pagaOrdine(@PathVariable UUID idOrdine) {
        OrdineDTO paidOrder = ordineService.pagaOrdine(idOrdine);
        log.info("Order with id={} paid",
                paidOrder.getIdOrdine());
        return paidOrder;
    }

    @GetMapping(path = "/{idOrdine}", produces = json)
    public OrdineDTO cercaOrdine(@PathVariable UUID idOrdine) {
        OrdineDTO searchedOrder = ordineService.cercaOrdine(idOrdine);
        log.info("Order lookup with id={}",
                searchedOrder.getIdOrdine());
        return searchedOrder;
    }

    @GetMapping(produces = json)
    public Collection<OrdineDTO> cercaTutti() {
        Collection<OrdineDTO> allOrders = ordineService.cercaTutti();
        log.info("Order listed");
        return allOrders;
    }

    @DeleteMapping(path = "/{idOrdine}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancellaOrdine(@PathVariable UUID idOrdine) {
        ordineService.cancellaOrdine(idOrdine);
        log.info("Order with id={} deleted",
                idOrdine);
    }

    @GetMapping(path = "/{idOrdine}/pagamenti", produces = json)
    public Collection<PagamentoDTO> pagamentiDellOrdine(@PathVariable UUID idOrdine) {
        Collection<PagamentoDTO> paymentsOfOrder = ordineService.pagamentiDellOrdine(idOrdine);
        log.info("Order payments search for order with id={}",
                idOrdine);
        return paymentsOfOrder;
    }

    @GetMapping(path = "/health")
    public void health() {}
}