package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import it.itsacademy.gestioneordinirestclient.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component @Transactional
@RequiredArgsConstructor
public class RicevitorePagamento {
    private final RepositoryOrdine repositoryOrdine;
    private final EmailService email;

    @RabbitListener(queues = {"payments.success.queue"})
    public void successfulPayment(UUID idOrdine) {
        Ordine ordine = repositoryOrdine.findByIdOrThrow(idOrdine);

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE)
            return;

        email.sendOrderPaymentSuccessMail("its-ordini-e-pagamenti-cri@mailinator.com", ordine.getDescrizione());

        ordine.setStatoOrdine(Ordine.StatoOrdine.PAGATO);
    }

    @RabbitListener(queues = {"payments.failure.queue"})
    public void failedPayment(UUID idOrdine) {
        Ordine ordine = repositoryOrdine.findByIdOrThrow(idOrdine);

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE)
            return;

        email.sendOrderPaymentFailMail("its-ordini-e-pagamenti-cri@mailinator.com", ordine.getDescrizione());

        ordine.setStatoOrdine(Ordine.StatoOrdine.DA_PAGARE);
    }
}
