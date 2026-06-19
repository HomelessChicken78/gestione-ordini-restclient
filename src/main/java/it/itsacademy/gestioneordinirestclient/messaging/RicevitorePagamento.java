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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevitorePagamento {
    private final RepositoryOrdine repositoryOrdine;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"payments.success.queue"})
    public void successfulPayment(UUID idOrdine) throws IOException  {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE)
            return;

        String fileName = "RICEVUTA_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (var writer = Files.newBufferedWriter(Path.of("/app/ricevute/" + fileName))) {
            writer.write("Bell'ordine bro");
            log.debug("Written on file");
        } catch (IOException e) {
            log.error("Error writing on the file.", e);
            throw e;
        }

        rabbitTemplate.convertAndSend("payments.exchange", "email.payment.accettato",
                new OrderPaymentEmailEvent(ordine.getIdOrdine(), ordine.getEmailCliente(), ordine.getDescrizione()));

        ordine.setStatoOrdine(Ordine.StatoOrdine.PAGATO);
    }

    @RabbitListener(queues = {"payments.failure.queue"})
    public void failedPayment(UUID idOrdine) {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        // Per sicurezza controlliamo che lo stato sia in elaborazione.
        // Infatti AMQ garantisce che il messaggio sia mandato almeno una volta, ma nulla vieta che venga inviato due volte.
        // NB: Non lanciamo eccezioni RabbitMQ penserebbe che ci sia stato un errore di elaborazione e rimetterebbe il messaggio in coda
        if (ordine.getStatoOrdine() != Ordine.StatoOrdine.IN_ELABORAZIONE)
            return;

        rabbitTemplate.convertAndSend("payments.exchange", "email.payment.rifiutato",
                new OrderPaymentEmailEvent(ordine.getIdOrdine(), ordine.getEmailCliente(), ordine.getDescrizione()));

        ordine.setStatoOrdine(Ordine.StatoOrdine.DA_PAGARE);
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
