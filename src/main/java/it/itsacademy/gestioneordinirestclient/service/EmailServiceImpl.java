package it.itsacademy.gestioneordinirestclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String sender;
    private final JavaMailSender mailSender;

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
    public void sendOrderPaymentSuccessMail(String recipient, String description) {
        String text = "Il tuo ordine " + description + " è stato pagato con successo!";
        sendMail(recipient, "Ordine pagato con successo", text);
    }

    @Override
    public void sendOrderPaymentFailMail(String recipient, String description) {
        String text = "Non è stato possibile pagare l'ordine " + description + " :(";
        sendMail(recipient, "Pagamento ordine fallito", text);
    }
}
