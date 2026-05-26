package it.itsacademy.gestioneordinirestclient.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class PagamentoDTO {
    private UUID idPagamento;

    private StatoPagamento statoPagamento;
    private Double totale = 0.0;
    private LocalDate dataPagamento = LocalDate.now();

    public enum StatoPagamento {
        RIFIUTATO, ACCETTATO
    }
}

