package com.autobots.automanager.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "clientes", itemRelation = "cliente")
public class ClienteSaida extends RepresentationModel<ClienteSaida> {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Date dataNascimento;
    private Date dataCadastro;
    private List<DocumentoSaida> documentos = new ArrayList<>();
    private EnderecoSaida endereco;
    private List<TelefoneSaida> telefones = new ArrayList<>();
}
