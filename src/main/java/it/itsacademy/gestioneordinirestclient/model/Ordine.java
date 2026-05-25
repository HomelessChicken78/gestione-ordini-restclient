package it.itsacademy.gestioneordinirestclient.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Ordine {
    @Id
    private UUID idOrdine;
    private Date dataCreazione = new Date();
    private String descrizione;
    private StatoOrdine statoOrdine = StatoOrdine.DAPAGARE;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
