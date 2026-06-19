package it.itsacademy.gestioneordinirestclient.mapper;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdineMapper {
    @Mapping(target = "idOrdine", ignore = true)
    @Mapping(target = "dataCreazione", ignore = true)
    @Mapping(target = "statoOrdine", ignore = true)
    @Mapping(target = "usernameCliente", ignore = true)
    @Mapping(target = "emailCliente", ignore = true)
    @Mapping(target = "nomeRicevuta", ignore = true)
    Ordine toEntity(CreaOrdineDTO nuovoOrdine);

    OrdineDTO toDTO(Ordine entity);

    Collection<OrdineDTO> toDTO(List<Ordine> entities);
}
