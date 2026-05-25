package it.itsacademy.gestioneordinirestclient.exception;

import it.itsacademy.gestioneordinirestclient.exception.dto.GeneralErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponseDTO> error402(PaymentRequiredException err402) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(new GeneralErrorResponseDTO(err402.getMessage(), 402));
    }

    @ExceptionHandler
    public ResponseEntity<GeneralErrorResponseDTO> error404Handler(NotFoundException err404) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Usando .notFound non posso mettere un body con .body perché non ritorna
                // un BodyBuilder (usato per il body) ma un HeadersBuilder (usato per gli headers)
                .body(new GeneralErrorResponseDTO(err404.getMessage(), 404));
    }
}
