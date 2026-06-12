package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.dto.OrderPaymentEmailEvent;
import it.itsacademy.gestioneordinirestclient.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RequiredArgsConstructor
public class RicevitoreMail {
    private final EmailService email;

    @RabbitListener(queues = {"payment.success.email"})
    public void sendPaymentSuccessEmail(OrderPaymentEmailEvent emailEvent) {
        email.sendOrderPaymentSuccessMail(emailEvent.getOrderId(), emailEvent.getEmail(), emailEvent.getOrderDescription());
    }

    @RabbitListener(queues = {"payment.failure.email"})
    public void sendPaymentFailureEmail(OrderPaymentEmailEvent emailEvent) {
        email.sendOrderPaymentFailMail(emailEvent.getOrderId(), emailEvent.getEmail(), emailEvent.getOrderDescription());
    }
}