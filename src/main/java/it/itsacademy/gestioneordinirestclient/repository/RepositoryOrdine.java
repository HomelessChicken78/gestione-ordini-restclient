package it.itsacademy.gestioneordinirestclient.repository;

import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryOrdine extends JpaRepository<Ordine, UUID> {
    default Ordine findByIdOrThrow(UUID idOrdine) {
        return findById(idOrdine)
                .orElseThrow(() -> new NotFoundException("Non esiste un ordine con id " + idOrdine));
    }
}
