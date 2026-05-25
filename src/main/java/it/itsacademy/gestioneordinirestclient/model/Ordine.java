package it.itsacademy.gestioneordinirestclient.model;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Ordine {
    private UUID idUtente;
    private Date dataCreazione;
    private String descrizione;
    private StatoOrdine statoOrdine;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
