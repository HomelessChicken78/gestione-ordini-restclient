package it.itsacademy.gestioneordinirestclient.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Ordine {
    private UUID idOrdine;
    private Date dataCreazione;
    private String descrizione;
    private StatoOrdine statoOrdine;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
