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

    @Column(nullable = false) private String usernameCliente;
    @Column(nullable = false) private String emailCliente;

    private String nomeRicevuta;

    public enum StatoOrdine {
        DA_PAGARE, PAGATO, ELIMINATO,
        IN_ELABORAZIONE, IN_ELABORAZIONE_CON_FILE // Stato temporaneo usato quando l'altro microservizio elabora la nostra richiesta di pagamento
    }
}
