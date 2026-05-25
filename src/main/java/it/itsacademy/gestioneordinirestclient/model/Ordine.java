package it.itsacademy.gestioneordinirestclient.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Ordine {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id private UUID idOrdine;
    private LocalDate dataCreazione = LocalDate.now();
    private String descrizione;
    private StatoOrdine statoOrdine = StatoOrdine.DAPAGARE;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
