package it.itsacademy.gestioneordinirestclient.dto;

import it.itsacademy.gestioneordinirestclient.model.Ordine;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class OrdineDTO {
    private UUID idOrdine;
    private Date dataCreazione;
    private String descrizione;
    private Ordine.StatoOrdine statoOrdine;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
