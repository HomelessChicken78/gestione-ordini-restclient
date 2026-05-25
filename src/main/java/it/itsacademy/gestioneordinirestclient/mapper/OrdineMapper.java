package it.itsacademy.gestioneordinirestclient.mapper;

import it.itsacademy.gestioneordinirestclient.dto.*;
import it.itsacademy.gestioneordinirestclient.model.Ordine;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrdineMapper {
    @Mapping(target = "idOrdine", ignore = true)
    @Mapping(target = "dataCreazione", ignore = true)
    @Mapping(target = "statoOrdine", ignore = true)
    Ordine toEntity(CreaOrdineDTO nuovoOrdine);

    OrdineDTO toDTO(Ordine entity);
}
