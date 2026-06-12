package it.itsacademy.gestioneordinirestclient.dto;

import lombok.*;
import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class OrderPaymentEmailEvent {
    private UUID orderId;
    private String email;
    private String orderDescription;
}