package it.itsacademy.gestioneordinirestclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String sender;
    private final JavaMailSender mailSender;

    // Formattatore umano: trasforma 2026-06-09T16:43:12.123456789 in "09/06/2026 16:43:12"
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void sendMail(String recipient, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(sender);
        msg.setTo(recipient);
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
    }

    @Override
    public void sendOrderPaymentSuccessMail(UUID idOrdine, String recipient, String description) {
        String dataFormattata = LocalDateTime.now().format(formatter);

        String text = "Gentile Cliente,\n\n" +
                "Ti confermiamo che il pagamento relativo al tuo ordine è stato elaborato con successo.\n\n" +
                "Dettagli della transazione:\n" +
                "----------------------------------------\n" +
                "Identificativo Ordine: " + description + "\n" +
                "Data e Ora Transazione: " + dataFormattata + "\n" +
                "Stato Pagamento: APPROVATO / ACCETTATO\n" +
                "----------------------------------------\n\n" +
                "Il nostro reparto logistico ha preso in carico la richiesta e sta provvedendo alla preparazione del pacco.\n" +
                "Riceverai una seconda comunicazione non appena la merce verrà affidata al corriere espresso.\n\n" +
                "Grazie per aver scelto i nostri servizi.\n\n" +
                "Servizio Clienti - ITS Academy Shop";

        sendMail(recipient, "Conferma Ricevuta Ordine " + idOrdine, text);
    }

    @Override
    public void sendOrderPaymentFailMail(UUID idOrdine, String recipient, String description) {
        String dataFormattata = LocalDateTime.now().format(formatter);

        String text = "Gentile Cliente,\n\n" +
                "Ti informiamo che l'istituto di credito ha acquistato o rifiutato la transazione relativa al tuo tentativo di acquisto.\n\n" +
                "Dettagli del tentativo di pagamento:\n" +
                "----------------------------------------\n" +
                "Identificativo Riferimento: " + description + "\n" +
                "Data e Ora Tentativo: " + dataFormattata + "\n" +
                "Stato Pagamento: RIFIUTATO / FALLITO\n" +
                "----------------------------------------\n\n" +
                "L'ordine è stato riportato temporaneamente in stato 'Da Pagare'.\n" +
                "Ti invitiamo a verificare la validità della carta utilizzata, la disponibilità dei fondi o a selezionare un metodo di pagamento alternativo per completare l'acquisto.\n\n" +
                "Se ritieni che si tratti di un errore, contatta il supporto della tua banca.\n\n" +
                "Cordiali saluti,\n" +
                "Supporto Amministrativo - ITS Academy Shop";

        sendMail(recipient, "Avviso di mancato pagamento per l'ordine " + idOrdine, text);
    }
}