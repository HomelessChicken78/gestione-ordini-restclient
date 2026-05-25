package it.itsacademy.gestioneordinirestclient.dto;

import it.itsacademy.gestioneordinirestclient.model.Ordine;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class OrdineDTO {
    private UUID idOrdine;
    private LocalDate dataCreazione;
    private String descrizione;
    private StatoOrdine statoOrdine;
    private Double totale;

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
