package it.itsacademy.gestioneordinirestclient.dto;

import lombok.*;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreaOrdineDTO {
    private String descrizione;
    private Double totale;
}
