package it.itsacademy.gestioneordinirestclient.dto;

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
    private String nomeRicevuta;

    public enum StatoOrdine {
        DA_PAGARE, PAGATO, ELIMINATO,
        IN_ELABORAZIONE // Stato temporaneo usato quando l'altro microservizio elabora la nostra richiesta di pagamento
    }
}
