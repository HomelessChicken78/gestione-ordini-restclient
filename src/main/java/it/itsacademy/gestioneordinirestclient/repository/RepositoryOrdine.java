package it.itsacademy.gestioneordinirestclient.repository;

import it.itsacademy.gestioneordinirestclient.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryOrdine extends JpaRepository<UUID, Ordine> {
}
