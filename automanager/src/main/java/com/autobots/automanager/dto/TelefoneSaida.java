package com.autobots.automanager.dto;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "telefones", itemRelation = "telefone")
public class TelefoneSaida extends RepresentationModel<TelefoneSaida> {
    private Long id;
    private String ddd;
    private String numero;
}
