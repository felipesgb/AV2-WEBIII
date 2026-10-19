package com.autobots.automanager.controles;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.dto.*;

/** Compatibilidade com a ATVI. Novos consumidores devem navegar pelas rotas plurais. */
@RestController
public class RotasLegadasControle {
    private final ClienteControle cliente;
    private final DocumentoControle documento;
    private final TelefoneControle telefone;
    private final EnderecoControle endereco;

    public RotasLegadasControle(ClienteControle cliente, DocumentoControle documento, TelefoneControle telefone, EnderecoControle endereco) {
        this.cliente = cliente;
        this.documento = documento;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    @GetMapping({"/cliente", "/cliente/clientes", "/clientes/clientes"})
    public CollectionModel<ClienteSaida> listarCliente() { return cliente.listar(); }

    @GetMapping({"/cliente/{id}", "/cliente/cliente/{id}", "/clientes/cliente/{id}"})
    public ClienteSaida buscarCliente(@PathVariable Long id) { return cliente.buscar(id); }

    @PostMapping({"/cliente", "/cliente/cadastro", "/clientes/cadastro"})
    public ResponseEntity<ClienteSaida> criarCliente(@RequestBody ClienteEntrada item) {
        return cliente.criar(item);
    }

    @PutMapping("/cliente/{id}")
    public ClienteSaida substituirCliente(@PathVariable Long id, @RequestBody ClienteEntrada item) {
        return cliente.atualizar(id, item);
    }

    @PatchMapping("/cliente/{id}")
    public ClienteSaida parcialCliente(@PathVariable Long id, @RequestBody ClienteEntrada item) {
        return cliente.atualizarParcial(id, item);
    }

    @PutMapping({"/cliente/atualizar", "/clientes/atualizar"})
    public ClienteSaida atualizarCliente(@RequestBody ClienteEntrada item) {
        return cliente.atualizarParcial(idObrigatorio(item.getId()), item);
    }

    @DeleteMapping("/cliente/{id}")
    public ResponseEntity<Void> excluirCliente(@PathVariable Long id) { return cliente.excluir(id); }

    @DeleteMapping({"/cliente/excluir", "/clientes/excluir"})
    public ResponseEntity<Void> excluirLegadoCliente(@RequestBody ClienteEntrada item) {
        return cliente.excluir(idObrigatorio(item.getId()));
    }

    @GetMapping({"/documento", "/documento/documentos", "/documentos/documentos"})
    public CollectionModel<DocumentoSaida> listarDocumento() { return documento.listar(); }

    @GetMapping({"/documento/{id}", "/documento/documento/{id}", "/documentos/documento/{id}"})
    public DocumentoSaida buscarDocumento(@PathVariable Long id) { return documento.buscar(id); }

    @PostMapping({"/documento", "/documento/cadastro", "/documentos/cadastro"})
    public ResponseEntity<DocumentoSaida> criarDocumento(@RequestBody DocumentoEntrada item, @RequestParam(required = false) Long clienteId) {
        return documento.criar(item, clienteId);
    }

    @PutMapping("/documento/{id}")
    public DocumentoSaida substituirDocumento(@PathVariable Long id, @RequestBody DocumentoEntrada item) {
        return documento.atualizar(id, item);
    }

    @PatchMapping("/documento/{id}")
    public DocumentoSaida parcialDocumento(@PathVariable Long id, @RequestBody DocumentoEntrada item) {
        return documento.atualizarParcial(id, item);
    }

    @PutMapping({"/documento/atualizar", "/documentos/atualizar"})
    public DocumentoSaida atualizarDocumento(@RequestBody DocumentoEntrada item) {
        return documento.atualizarParcial(idObrigatorio(item.getId()), item);
    }

    @DeleteMapping("/documento/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable Long id) { return documento.excluir(id); }

    @DeleteMapping({"/documento/excluir", "/documentos/excluir"})
    public ResponseEntity<Void> excluirLegadoDocumento(@RequestBody DocumentoEntrada item) {
        return documento.excluir(idObrigatorio(item.getId()));
    }

    @GetMapping({"/telefone", "/telefone/telefones", "/telefones/telefones"})
    public CollectionModel<TelefoneSaida> listarTelefone() { return telefone.listar(); }

    @GetMapping({"/telefone/{id}", "/telefone/telefone/{id}", "/telefones/telefone/{id}"})
    public TelefoneSaida buscarTelefone(@PathVariable Long id) { return telefone.buscar(id); }

    @PostMapping({"/telefone", "/telefone/cadastro", "/telefones/cadastro"})
    public ResponseEntity<TelefoneSaida> criarTelefone(@RequestBody TelefoneEntrada item, @RequestParam(required = false) Long clienteId) {
        return telefone.criar(item, clienteId);
    }

    @PutMapping("/telefone/{id}")
    public TelefoneSaida substituirTelefone(@PathVariable Long id, @RequestBody TelefoneEntrada item) {
        return telefone.atualizar(id, item);
    }

    @PatchMapping("/telefone/{id}")
    public TelefoneSaida parcialTelefone(@PathVariable Long id, @RequestBody TelefoneEntrada item) {
        return telefone.atualizarParcial(id, item);
    }

    @PutMapping({"/telefone/atualizar", "/telefones/atualizar"})
    public TelefoneSaida atualizarTelefone(@RequestBody TelefoneEntrada item) {
        return telefone.atualizarParcial(idObrigatorio(item.getId()), item);
    }

    @DeleteMapping("/telefone/{id}")
    public ResponseEntity<Void> excluirTelefone(@PathVariable Long id) { return telefone.excluir(id); }

    @DeleteMapping({"/telefone/excluir", "/telefones/excluir"})
    public ResponseEntity<Void> excluirLegadoTelefone(@RequestBody TelefoneEntrada item) {
        return telefone.excluir(idObrigatorio(item.getId()));
    }

    @GetMapping({"/endereco", "/endereco/enderecos", "/enderecos/enderecos"})
    public CollectionModel<EnderecoSaida> listarEndereco() { return endereco.listar(); }

    @GetMapping({"/endereco/{id}", "/endereco/endereco/{id}", "/enderecos/endereco/{id}"})
    public EnderecoSaida buscarEndereco(@PathVariable Long id) { return endereco.buscar(id); }

    @PostMapping({"/endereco", "/endereco/cadastro", "/enderecos/cadastro"})
    public ResponseEntity<EnderecoSaida> criarEndereco(@RequestBody EnderecoEntrada item, @RequestParam(required = false) Long clienteId) {
        return endereco.criar(item, clienteId);
    }

    @PutMapping("/endereco/{id}")
    public EnderecoSaida substituirEndereco(@PathVariable Long id, @RequestBody EnderecoEntrada item) {
        return endereco.atualizar(id, item);
    }

    @PatchMapping("/endereco/{id}")
    public EnderecoSaida parcialEndereco(@PathVariable Long id, @RequestBody EnderecoEntrada item) {
        return endereco.atualizarParcial(id, item);
    }

    @PutMapping({"/endereco/atualizar", "/enderecos/atualizar"})
    public EnderecoSaida atualizarEndereco(@RequestBody EnderecoEntrada item) {
        return endereco.atualizarParcial(idObrigatorio(item.getId()), item);
    }

    @DeleteMapping("/endereco/{id}")
    public ResponseEntity<Void> excluirEndereco(@PathVariable Long id) { return endereco.excluir(id); }

    @DeleteMapping({"/endereco/excluir", "/enderecos/excluir"})
    public ResponseEntity<Void> excluirLegadoEndereco(@RequestBody EnderecoEntrada item) {
        return endereco.excluir(idObrigatorio(item.getId()));
    }

    private Long idObrigatorio(Long id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID");
        return id;
    }
}
