package com.autobots.automanager.controles;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.dto.EnderecoEntrada;
import com.autobots.automanager.dto.EnderecoSaida;
import com.autobots.automanager.dto.ConversorDto;
import com.autobots.automanager.modelo.Hipermidia;
import com.autobots.automanager.servicos.EnderecoServico;

@RestController
@RequestMapping("/enderecos")
public class EnderecoControle {
    private final EnderecoServico servico;
    private final Hipermidia hipermidia;

    public EnderecoControle(EnderecoServico servico, Hipermidia hipermidia) {
        this.servico = servico;
        this.hipermidia = hipermidia;
    }

    @GetMapping
    public CollectionModel<EnderecoSaida> listar() {
        return hipermidia.enderecos(servico.listar());
    }

    @GetMapping("/{id}")
    public EnderecoSaida buscar(@PathVariable Long id) {
        return hipermidia.recurso(servico.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EnderecoSaida> criar(@RequestBody EnderecoEntrada item, @RequestParam(required = false) Long clienteId) {
        EnderecoSaida criado = hipermidia.recurso(servico.criar(ConversorDto.paraEntidade(item), clienteId));
        return ResponseEntity.created(criado.getRequiredLink("self").toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public EnderecoSaida atualizar(@PathVariable Long id, @RequestBody EnderecoEntrada item) {
        return hipermidia.recurso(servico.substituir(id, ConversorDto.paraEntidade(item)));
    }

    @PatchMapping("/{id}")
    public EnderecoSaida atualizarParcial(@PathVariable Long id, @RequestBody EnderecoEntrada item) {
        return hipermidia.recurso(servico.atualizar(id, ConversorDto.paraEntidade(item)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
