package com.autobots.automanager.controles;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.dto.DocumentoEntrada;
import com.autobots.automanager.dto.DocumentoSaida;
import com.autobots.automanager.dto.ConversorDto;
import com.autobots.automanager.modelo.Hipermidia;
import com.autobots.automanager.servicos.DocumentoServico;

@RestController
@RequestMapping("/documentos")
public class DocumentoControle {
    private final DocumentoServico servico;
    private final Hipermidia hipermidia;

    public DocumentoControle(DocumentoServico servico, Hipermidia hipermidia) {
        this.servico = servico;
        this.hipermidia = hipermidia;
    }

    @GetMapping
    public CollectionModel<DocumentoSaida> listar() {
        return hipermidia.documentos(servico.listar());
    }

    @GetMapping("/{id}")
    public DocumentoSaida buscar(@PathVariable Long id) {
        return hipermidia.recurso(servico.buscar(id));
    }

    @PostMapping
    public ResponseEntity<DocumentoSaida> criar(@RequestBody DocumentoEntrada item, @RequestParam(required = false) Long clienteId) {
        DocumentoSaida criado = hipermidia.recurso(servico.criar(ConversorDto.paraEntidade(item), clienteId));
        return ResponseEntity.created(criado.getRequiredLink("self").toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public DocumentoSaida atualizar(@PathVariable Long id, @RequestBody DocumentoEntrada item) {
        return hipermidia.recurso(servico.substituir(id, ConversorDto.paraEntidade(item)));
    }

    @PatchMapping("/{id}")
    public DocumentoSaida atualizarParcial(@PathVariable Long id, @RequestBody DocumentoEntrada item) {
        return hipermidia.recurso(servico.atualizar(id, ConversorDto.paraEntidade(item)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
