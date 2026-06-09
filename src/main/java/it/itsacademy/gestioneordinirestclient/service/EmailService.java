package it.itsacademy.gestioneordinirestclient.service;

import java.util.UUID;

public interface EmailService {
    void sendMail(String recipient, String subject, String text);

    void sendOrderPaymentSuccessMail(UUID idOrdine, String recipient, String description);

    void sendOrderPaymentFailMail(UUID idOrdine, String recipient, String description);
}
