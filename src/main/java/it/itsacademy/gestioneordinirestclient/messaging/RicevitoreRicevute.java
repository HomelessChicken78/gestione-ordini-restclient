package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.repository.RepositoryOrdine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;

@Component @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevitoreRicevute {
    private final RepositoryOrdine repositoryOrdine;
    private final S3AsyncClient s3;

    @Value("${features.s3.receipt-upload-enabled}")
    private boolean isS3UploadEnabled;

    @Value("${reports.template.directory}")
    private String jasperTemplateDir;

    @Value("${reports.template.filename}")
    private String jasperTemplateFile;

    @Value("${s3.bucket.name}")
    private String bucketS3;

    @Value("${s3.bucket.prefixes.root}")
    private String rootPrefix;

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
    }

    @RabbitListener(queues = {"receipts.pdf.queue"})
    public void createReceiptPdf(UUID idOrdine) throws IOException, JRException {
        Ordine ordine = findByIdOrLogAndThrow(idOrdine);

        String reportFileName = "REPORT_" + now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try (InputStream stream = new FileInputStream(jasperTemplateDir + jasperTemplateFile + ".jrxml")) {
            log.trace("Creating report.");
            var report = JasperCompileManager.compileReport(stream);
            log.debug("Compiled jrxml.");

            JRSaver.saveObject(report, jasperTemplateDir + jasperTemplateFile + ".jasper");
            log.trace("Saved .jasper file.");

            Map<String, Object> params = new HashMap<>();

            params.put("idOrdine", ordine.getIdOrdine().toString());
            params.put("dataCreazione", now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            params.put("cliente", ordine.getUsernameCliente() + " (" + ordine.getEmailCliente() + ")");
            params.put("descrizione", ordine.getDescrizione());
            params.put("totale", "€ " + ordine.getTotale());

            try (InputStream fileLogo = new FileInputStream(jasperTemplateDir + "logo.png")) {
                params.put("logo", fileLogo);

                JasperPrint print = JasperFillManager.fillReport(report, params, new net.sf.jasperreports.engine.JREmptyDataSource());
                JasperExportManager.exportReportToPdfFile(print, "/app/ricevute/" + reportFileName + ".pdf");

                if (isS3UploadEnabled) {
                    final String s3ObjectKey = rootPrefix + "/" + ordine.getUsernameCliente() + "/" + reportFileName + ".pdf"; // Che nome dare all'oggetto s3
                    final Path reportFromPath = Paths.get("/app/ricevute/" + reportFileName + ".pdf");

                    s3.putObject(b -> b.bucket(bucketS3).key(s3ObjectKey).contentType("application/pdf").build(),
                        AsyncRequestBody.fromFile(reportFromPath)
                    )
                    .whenComplete((response, exception) -> {
                        // ATTENZIONE: Questo blocco di codice NON viene eseguito dal thread principale.
                        // Verrà eseguito in futuro dal thread di Netty che riceve la risposta da AWS.
                        if (exception != null)
                            log.error("[Thread: {}] Error during the upload of {}: {}",
                                Thread.currentThread().getName(), s3ObjectKey, exception.getMessage()
                            );
                        else
                            log.info("[Thread: {}] File uploaded correctly. ETag: {}",
                                Thread.currentThread().getName(), response.eTag()
                            );
                    });
                }
            } catch (IOException e) {
                log.error("Error finding logo file", e);
                throw e;
            }
        } catch (JRException e) {
            log.error("Error creating report.", e);
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
