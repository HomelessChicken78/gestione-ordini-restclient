package it.itsacademy.gestioneordinirestclient.repository;

import it.itsacademy.gestioneordinirestclient.exception.NotFoundException;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.UUID;

public interface RepositoryOrdine extends JpaRepository<Ordine, UUID> {
    default Ordine findByIdOrThrow(UUID idOrdine) {
        return findById(idOrdine)
                .filter(o -> o.getStatoOrdine() != Ordine.StatoOrdine.ELIMINATO)
                .orElseThrow(() -> new NotFoundException("Non esiste un ordine con id " + idOrdine));
    }

    @Query("SELECT o FROM Ordine AS o WHERE o.statoOrdine <> 'ELIMINATO'")
    Collection<Ordine> findAllNotDeleted();
}
