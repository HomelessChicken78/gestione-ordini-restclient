package it.itsacademy.gestioneordinirestclient.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class OrdineDTO {
    private UUID idOrdine;
    private LocalDate dataCreazione;
    private String descrizione;
    private StatoOrdine statoOrdine;
    private Double totale;
    private Collection<PagamentoDTO> pagamenti = new ArrayList<>();

    public enum StatoOrdine {
        DAPAGARE, PAGATO, ELIMINATO
    }
}
