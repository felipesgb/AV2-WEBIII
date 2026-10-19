package com.autobots.automanager.dto;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "documentos", itemRelation = "documento")
public class DocumentoSaida extends RepresentationModel<DocumentoSaida> {
    private Long id;
    private String tipo;
    private String numero;
}
