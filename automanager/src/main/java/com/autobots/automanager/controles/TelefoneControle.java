package com.autobots.automanager.controles;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.dto.TelefoneEntrada;
import com.autobots.automanager.dto.TelefoneSaida;
import com.autobots.automanager.dto.ConversorDto;
import com.autobots.automanager.modelo.Hipermidia;
import com.autobots.automanager.servicos.TelefoneServico;

@RestController
@RequestMapping("/telefones")
public class TelefoneControle {
    private final TelefoneServico servico;
    private final Hipermidia hipermidia;

    public TelefoneControle(TelefoneServico servico, Hipermidia hipermidia) {
        this.servico = servico;
        this.hipermidia = hipermidia;
    }

    @GetMapping
    public CollectionModel<TelefoneSaida> listar() {
        return hipermidia.telefones(servico.listar());
    }

    @GetMapping("/{id}")
    public TelefoneSaida buscar(@PathVariable Long id) {
        return hipermidia.recurso(servico.buscar(id));
    }

    @PostMapping
    public ResponseEntity<TelefoneSaida> criar(@RequestBody TelefoneEntrada item, @RequestParam(required = false) Long clienteId) {
        TelefoneSaida criado = hipermidia.recurso(servico.criar(ConversorDto.paraEntidade(item), clienteId));
        return ResponseEntity.created(criado.getRequiredLink("self").toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public TelefoneSaida atualizar(@PathVariable Long id, @RequestBody TelefoneEntrada item) {
        return hipermidia.recurso(servico.substituir(id, ConversorDto.paraEntidade(item)));
    }

    @PatchMapping("/{id}")
    public TelefoneSaida atualizarParcial(@PathVariable Long id, @RequestBody TelefoneEntrada item) {
        return hipermidia.recurso(servico.atualizar(id, ConversorDto.paraEntidade(item)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
