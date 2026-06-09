package it.itsacademy.gestioneordinirestclient.service;

public interface EmailService {
    void sendMail(String recipient, String subject, String text);

    void sendOrderPaymentSuccessMail(String recipient, String description);

    void sendOrderPaymentFailMail(String recipient, String description);
}
