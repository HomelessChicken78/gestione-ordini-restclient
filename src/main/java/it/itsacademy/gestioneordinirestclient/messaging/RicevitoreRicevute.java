package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRSaver;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;

@Component @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevitoreRicevute {
    private final RepositoryOrdine repositoryOrdine;

    @Value("${reports.template.directory}")
    private String jasperTemplateDir;

    @Value("${reports.template.filename}")
    private String jasperTemplateFile;

    private final SessionFactory sessionFactory;

    @RabbitListener(queues = {"receipts.file.queue"})
    public void createReceiptFile(UUID idOrdine) throws IOException {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        String ricevutaFileName = "RICEVUTA_" + now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (var writer = Files.newBufferedWriter(Path.of("/app/ricevute/" + ricevutaFileName))) {
            String receiptMsg = "L'utente " + ordine.getUsernameCliente()
                    + " in data " + now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " ha pagato " + ordine.getTotale() + "€";
            log.info("Creating receipt. file_name={}, message={}", ricevutaFileName, receiptMsg);

            writer.write(receiptMsg);
            log.debug("Written on file");

            ordine.setNomeRicevuta(ricevutaFileName);
            log.debug("Set value for \"nomeRicevuta\" of order. nomeRicevuta={}, orderId={}", ordine.getNomeRicevuta(), ordine.getIdOrdine());
        } catch (IOException e) {
            log.error("Error writing on the file.", e);
            throw e;
        }

        String reportFileName = "REPORT_" + now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try (InputStream stream = new FileInputStream(jasperTemplateDir + jasperTemplateFile + ".jrxml")) {
            log.trace("Creating report.");
            var report = JasperCompileManager.compileReport(stream);
            log.debug("Compiled jrxml.");

            JRSaver.saveObject(report, jasperTemplateDir + jasperTemplateFile + ".jasper");
            log.trace("Saved .jasper file.");

            Map<String, Object> params = new HashMap<>();
            params.put("idOrdine", ordine.getIdOrdine().toString()); // Convertito in Stringa!
            params.put("dataCreazione", now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            params.put("cliente", ordine.getUsernameCliente() + " (" + ordine.getEmailCliente() + ")");
            params.put("descrizione", ordine.getDescrizione());
            params.put("totale", "€ " + ordine.getTotale());

            JasperPrint print = JasperFillManager.fillReport(report, params, new net.sf.jasperreports.engine.JREmptyDataSource());

            JasperExportManager.exportReportToPdfFile(print, "/app/ricevute/" + reportFileName + ".pdf");
            JasperExportManager.exportReportToPdfFile(print, "/app/ricevute/" + reportFileName + ".pdf");
        } catch (JRException e) {
            log.error("Error creating report.", e);
            throw new RuntimeException(e);
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
