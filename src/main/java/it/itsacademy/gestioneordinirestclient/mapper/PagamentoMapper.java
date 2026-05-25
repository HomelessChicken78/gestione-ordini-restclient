package it.itsacademy.gestioneordinirestclient.mapper;

import it.itsacademy.gestioneordinirestclient.dto.PagamentoDTO;
import it.itsacademy.gestioneordinirestclient.model.Pagamento;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {
    PagamentoDTO toDTO(Pagamento entity);
    Pagamento toEntity(PagamentoDTO risposta);
}
