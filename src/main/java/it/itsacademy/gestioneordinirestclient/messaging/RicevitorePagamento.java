package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.dto.OrderPaymentEmailEvent;
import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor @Slf4j
// NOTA BENE: Questo metodo è volutamente NON @Transactional.
// Se fosse @Transactional, il commit sul DB avverrebbe solo alla fine del metodo.
// Di conseguenza, i messaggi RabbitMQ farebbero partire i worker asincroni (es. ricevute)
// prima del commit. I worker leggerebbero lo stato vecchio (IN_ELABORAZIONE) dal DB
// e, al momento del loro salvataggio, andrebbero a sovrascrivere accidentalmente
// lo stato PAGATO (Race condition / Lost update).
// Rimuovendo @Transactional sfruttiamo l'auto-commit di JPA per salvare lo stato PAGATO
// prima di inviare i messaggi in coda.
// Drawback: la transazione è solo sul save e non è atomica. Per tale motivo questa non è la soluzione corretta
public class RicevitorePagamento {
    private final RepositoryOrdine repositoryOrdine;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"payments.success.queue"})
    public void successfulPayment(UUID idOrdine)  {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        Ordine.StatoOrdine statoAttuale = ordine.getStatoOrdine();

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (statoAttuale != Ordine.StatoOrdine.IN_ELABORAZIONE &&
                statoAttuale != Ordine.StatoOrdine.IN_ELABORAZIONE_CON_FILE) {
            log.debug("Ignored success message for order id={} because status is {}", idOrdine, statoAttuale);
            return;
        }

        ordine.setStatoOrdine(Ordine.StatoOrdine.PAGATO);
        repositoryOrdine.save(ordine);

        // Generiamo la ricevuta se non è stato caricato un file dall'utente
        if (statoAttuale == Ordine.StatoOrdine.IN_ELABORAZIONE) {
            log.debug("Triggering receipt creation for order id={}", idOrdine);
            rabbitTemplate.convertAndSend("receipts.exchange", "receipts.file.create", idOrdine);
            rabbitTemplate.convertAndSend("receipts.exchange", "receipts.pdf.create", idOrdine);
        } else log.debug("Skipped receipt creation for order id={} because a file was already uploaded.", idOrdine);

        rabbitTemplate.convertAndSend("payments.exchange", "email.payment.accettato",
                new OrderPaymentEmailEvent(ordine.getIdOrdine(), ordine.getEmailCliente(), ordine.getDescrizione()));
    }

    @RabbitListener(queues = {"payments.failure.queue"})
    public void failedPayment(UUID idOrdine) {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE &&
                ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE_CON_FILE) {
            log.debug("Ignored failure message for order id={} because status is {}", idOrdine, ordine.getStatoOrdine());
            return;
        }

        ordine.setStatoOrdine(Ordine.StatoOrdine.DA_PAGARE);
        repositoryOrdine.save(ordine);

        rabbitTemplate.convertAndSend("payments.exchange", "email.payment.rifiutato",
                new OrderPaymentEmailEvent(ordine.getIdOrdine(), ordine.getEmailCliente(), ordine.getDescrizione()));
    }

    private Ordine findByIdOrLogAndThrow(UUID idOrdine) {
        try {
            return repositoryOrdine.findByIdOrThrow(idOrdine);
        } catch (NotFoundException e) {
            log.warn("Order not found. id={}", idOrdine);
            throw e;
        }
    }
}