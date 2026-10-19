package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioControle {
    @GetMapping("/")
    public RepresentationModel<?> inicio() {
        return new RepresentationModel<>().add(
            linkTo(methodOn(InicioControle.class).inicio()).withSelfRel(),
            linkTo(methodOn(ClienteControle.class).listar()).withRel("clientes"),
            linkTo(methodOn(DocumentoControle.class).listar()).withRel("documentos"),
            linkTo(methodOn(TelefoneControle.class).listar()).withRel("telefones"),
            linkTo(methodOn(EnderecoControle.class).listar()).withRel("enderecos"));
    }
}
