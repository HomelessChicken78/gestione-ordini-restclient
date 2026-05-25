package it.itsacademy.gestioneordinirestclient.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class PagamentoDTO {
    private UUID idPagamento;

    private StatoPagamento statoPagamento;

    public enum StatoPagamento {
        RIFIUTATO, ACCETTATO
    }
}

