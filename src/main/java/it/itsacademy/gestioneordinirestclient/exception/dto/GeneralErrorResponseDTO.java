package it.itsacademy.gestioneordinirestclient.exception.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralErrorResponseDTO {
    private String message;

    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer status = 400;

    public GeneralErrorResponseDTO(String message) {
        this.message = message;
    }

    public GeneralErrorResponseDTO(String message, Integer status) {
        this.message = message;
        this.status = status;
    }
}