package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.model.Ordine;
import it.itsacademy.gestioneordinirestclient.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.UUID;

@RequiredArgsConstructor
public class RicevitoreMail {
    private final EmailService email;

    @RabbitListener(queues = {"payment.success.email"})
    public void sendPaymentSuccessEmail(UUID idOrdine, String recipient, String description) {
        email.sendOrderPaymentSuccessMail(idOrdine, recipient, description);
    }

    @RabbitListener(queues = {"payment.failure.email"})
    public void sendPaymentFailureEmail(UUID idOrdine, String recipient, String description) {
        email.sendOrderPaymentFailMail(idOrdine, recipient, description);
    }
}