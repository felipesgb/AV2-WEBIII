package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;
import com.autobots.automanager.controles.*;
import com.autobots.automanager.dto.*;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.VinculoCliente;

/** Cria representacoes novas; nunca adiciona links nas entidades do banco. */
@Component
public class Hipermidia {
    private final ClienteRepositorio clientes;

    public Hipermidia(ClienteRepositorio clientes) { this.clientes = clientes; }

    private <T extends RepresentationModel<T>> T comuns(T dto, Link self, Link colecao,
            Link atualizar, Link excluir, Long clienteId) {
        dto.add(self.withSelfRel(), colecao.withRel("collection"), atualizar.withRel("atualizar"),
            excluir.withRel("excluir"), linkTo(methodOn(InicioControle.class).inicio()).withRel("inicio"));
        if (clienteId != null) {
            dto.add(linkTo(methodOn(ClienteControle.class).buscar(clienteId)).withRel("cliente"));
        }
        return dto;
    }

    private <T extends RepresentationModel<T>> CollectionModel<T> colecao(
            List<T> itens, Class<T> tipo, Link self, Link cadastrar) {
        return CollectionModel.of(itens).withFallbackType(tipo).add(self.withSelfRel(),
            cadastrar.withRel("cadastrar").expand(), linkTo(methodOn(InicioControle.class).inicio()).withRel("inicio"));
    }

    private Map<Long, Long> donos(List<VinculoCliente> vinculos) {
        return vinculos.stream().collect(Collectors.toMap(VinculoCliente::getRecursoId, VinculoCliente::getClienteId));
    }

    public ClienteSaida recurso(Cliente cliente) {
        Long id = cliente.getId();
        ClienteSaida dto = ConversorDto.paraSaida(cliente);
        dto.setDocumentos(cliente.getDocumentos().stream().map(d -> recurso(d, id)).toList());
        dto.setTelefones(cliente.getTelefones().stream().map(t -> recurso(t, id)).toList());
        if (cliente.getEndereco() == null) {
            dto.add(linkTo(methodOn(EnderecoControle.class).criar(null, id)).withRel("cadastrar-endereco"));
        } else {
            dto.setEndereco(recurso(cliente.getEndereco(), id));
            dto.add(linkTo(methodOn(EnderecoControle.class).buscar(cliente.getEndereco().getId())).withRel("endereco"));
        }
        dto.add(linkTo(methodOn(DocumentoControle.class).criar(null, id)).withRel("cadastrar-documento"),
            linkTo(methodOn(TelefoneControle.class).criar(null, id)).withRel("cadastrar-telefone"));
        return comuns(dto, linkTo(methodOn(ClienteControle.class).buscar(id)).withSelfRel(),
            linkTo(methodOn(ClienteControle.class).listar()).withSelfRel(),
            linkTo(methodOn(ClienteControle.class).atualizarParcial(id, null)).withSelfRel(),
            linkTo(methodOn(ClienteControle.class).excluir(id)).withSelfRel(), null);
    }

    public CollectionModel<ClienteSaida> clientes(List<Cliente> itens) {
        return colecao(itens.stream().map(this::recurso).toList(), ClienteSaida.class,
            linkTo(methodOn(ClienteControle.class).listar()).withSelfRel(),
            linkTo(methodOn(ClienteControle.class).criar(null)).withRel("cadastrar"));
    }

    public DocumentoSaida recurso(Documento entidade) {
        Long clienteId = donos(clientes.donosDocumento(List.of(entidade.getId()))).get(entidade.getId());
        return recurso(entidade, clienteId);
    }

    private DocumentoSaida recurso(Documento entidade, Long clienteId) {
        Long id = entidade.getId();
        return comuns(ConversorDto.paraSaida(entidade),
            linkTo(methodOn(DocumentoControle.class).buscar(id)).withSelfRel(),
            linkTo(methodOn(DocumentoControle.class).listar()).withSelfRel(),
            linkTo(methodOn(DocumentoControle.class).atualizarParcial(id, null)).withSelfRel(),
            linkTo(methodOn(DocumentoControle.class).excluir(id)).withSelfRel(), clienteId);
    }

    public CollectionModel<DocumentoSaida> documentos(List<Documento> itens) {
        // Uma consulta de vinculos para toda a lista, em vez de uma por item.
        Map<Long, Long> clientesPorId = itens.isEmpty() ? Map.of() : donos(
            clientes.donosDocumento(itens.stream().map(Documento::getId).toList()));
        return colecao(itens.stream().map(e -> recurso(e, clientesPorId.get(e.getId()))).toList(), DocumentoSaida.class,
            linkTo(methodOn(DocumentoControle.class).listar()).withSelfRel(),
            linkTo(methodOn(DocumentoControle.class).criar(null, null)).withRel("cadastrar"));
    }

    public TelefoneSaida recurso(Telefone entidade) {
        Long clienteId = donos(clientes.donosTelefone(List.of(entidade.getId()))).get(entidade.getId());
        return recurso(entidade, clienteId);
    }

    private TelefoneSaida recurso(Telefone entidade, Long clienteId) {
        Long id = entidade.getId();
        return comuns(ConversorDto.paraSaida(entidade),
            linkTo(methodOn(TelefoneControle.class).buscar(id)).withSelfRel(),
            linkTo(methodOn(TelefoneControle.class).listar()).withSelfRel(),
            linkTo(methodOn(TelefoneControle.class).atualizarParcial(id, null)).withSelfRel(),
            linkTo(methodOn(TelefoneControle.class).excluir(id)).withSelfRel(), clienteId);
    }

    public CollectionModel<TelefoneSaida> telefones(List<Telefone> itens) {
        // Uma consulta de vinculos para toda a lista, em vez de uma por item.
        Map<Long, Long> clientesPorId = itens.isEmpty() ? Map.of() : donos(
            clientes.donosTelefone(itens.stream().map(Telefone::getId).toList()));
        return colecao(itens.stream().map(e -> recurso(e, clientesPorId.get(e.getId()))).toList(), TelefoneSaida.class,
            linkTo(methodOn(TelefoneControle.class).listar()).withSelfRel(),
            linkTo(methodOn(TelefoneControle.class).criar(null, null)).withRel("cadastrar"));
    }

    public EnderecoSaida recurso(Endereco entidade) {
        Long clienteId = donos(clientes.donosEndereco(List.of(entidade.getId()))).get(entidade.getId());
        return recurso(entidade, clienteId);
    }

    private EnderecoSaida recurso(Endereco entidade, Long clienteId) {
        Long id = entidade.getId();
        return comuns(ConversorDto.paraSaida(entidade),
            linkTo(methodOn(EnderecoControle.class).buscar(id)).withSelfRel(),
            linkTo(methodOn(EnderecoControle.class).listar()).withSelfRel(),
            linkTo(methodOn(EnderecoControle.class).atualizarParcial(id, null)).withSelfRel(),
            linkTo(methodOn(EnderecoControle.class).excluir(id)).withSelfRel(), clienteId);
    }

    public CollectionModel<EnderecoSaida> enderecos(List<Endereco> itens) {
        // Uma consulta de vinculos para toda a lista, em vez de uma por item.
        Map<Long, Long> clientesPorId = itens.isEmpty() ? Map.of() : donos(
            clientes.donosEndereco(itens.stream().map(Endereco::getId).toList()));
        return colecao(itens.stream().map(e -> recurso(e, clientesPorId.get(e.getId()))).toList(), EnderecoSaida.class,
            linkTo(methodOn(EnderecoControle.class).listar()).withSelfRel(),
            linkTo(methodOn(EnderecoControle.class).criar(null, null)).withRel("cadastrar"));
    }
}
