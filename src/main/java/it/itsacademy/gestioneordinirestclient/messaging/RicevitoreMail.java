package it.itsacademy.gestioneordinirestclient.messaging;

import it.itsacademy.gestioneordinirestclient.dto.OrderPaymentEmailEvent;
import it.itsacademy.gestioneordinirestclient.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Transactional
@RequiredArgsConstructor @Slf4j
public class RicevitoreMail {
    private final EmailService email;

    @RabbitListener(queues = {"payment.success.email"})
    public void sendPaymentSuccessEmail(OrderPaymentEmailEvent emailEvent) {
        log.info("Sending success email. recipient={}", emailEvent.getEmail());
        email.sendOrderPaymentSuccessMail(emailEvent.getOrderId(), emailEvent.getEmail(), emailEvent.getOrderDescription());
        log.debug("Sent email. recipient={}", emailEvent.getEmail());
    }

    @RabbitListener(queues = {"payment.failure.email"})
    public void sendPaymentFailureEmail(OrderPaymentEmailEvent emailEvent) {
        log.info("Sending failure email. recipient={}", emailEvent.getEmail());
        email.sendOrderPaymentFailMail(emailEvent.getOrderId(), emailEvent.getEmail(), emailEvent.getOrderDescription());
        log.debug("Sent email. recipient={}", emailEvent.getEmail());
    }
}