package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.time.LocalDateTime.now;

@Component @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevitoreRicevute {
    private final RepositoryOrdine repositoryOrdine;

    @RabbitListener(queues = {"receipts.file.queue"})
    public void createReceiptFile(UUID idOrdine) throws IOException {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        String fileName = "RICEVUTA_" + now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (var writer = Files.newBufferedWriter(Path.of("/app/ricevute/" + fileName))) {
            String receiptMsg = "L'utente " + ordine.getUsernameCliente()
                    + " in data " + now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " ha pagato " + ordine.getTotale() + "€";
            log.info("Creating receipt. file_name={}, message={}", fileName, receiptMsg);

            writer.write(receiptMsg);
            log.debug("Written on file");

            ordine.setNomeRicevuta(fileName);
            log.debug("Set value for \"nomeRicevuta\" of order. nomeRicevuta={}, orderId={}", ordine.getNomeRicevuta(), ordine.getIdOrdine());
        } catch (IOException e) {
            log.error("Error writing on the file.", e);
            throw e;
        }
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
