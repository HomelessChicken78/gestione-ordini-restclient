package it.itsacademy.gestioneordinirestclient.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Ordine {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id private UUID idOrdine;
    private LocalDate dataCreazione = LocalDate.now();
    private String descrizione;

    @Enumerated(EnumType.STRING)
    private StatoOrdine statoOrdine = StatoOrdine.DA_PAGARE;
    private Double totale;

    public enum StatoOrdine {
        DA_PAGARE, PAGATO, ELIMINATO,
        IN_ELABORAZIONE // Stato temporaneo usato quando l'altro microservizio elabora la nostra richiesta di pagamento
    }
}
