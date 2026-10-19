package com.autobots.automanager.controles;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.autobots.automanager.dto.ClienteEntrada;
import com.autobots.automanager.dto.ClienteSaida;
import com.autobots.automanager.dto.ConversorDto;
import com.autobots.automanager.modelo.Hipermidia;
import com.autobots.automanager.servicos.ClienteServico;

@RestController
@RequestMapping("/clientes")
public class ClienteControle {
    private final ClienteServico servico;
    private final Hipermidia hipermidia;

    public ClienteControle(ClienteServico servico, Hipermidia hipermidia) {
        this.servico = servico;
        this.hipermidia = hipermidia;
    }

    @GetMapping
    public CollectionModel<ClienteSaida> listar() {
        return hipermidia.clientes(servico.listar());
    }

    @GetMapping("/{id}")
    public ClienteSaida buscar(@PathVariable Long id) {
        return hipermidia.recurso(servico.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ClienteSaida> criar(@RequestBody ClienteEntrada item) {
        ClienteSaida criado = hipermidia.recurso(servico.criar(ConversorDto.paraEntidade(item)));
        return ResponseEntity.created(criado.getRequiredLink("self").toUri()).body(criado);
    }

    @PutMapping("/{id}")
    public ClienteSaida atualizar(@PathVariable Long id, @RequestBody ClienteEntrada item) {
        return hipermidia.recurso(servico.substituir(id, ConversorDto.paraEntidade(item)));
    }

    @PatchMapping("/{id}")
    public ClienteSaida atualizarParcial(@PathVariable Long id, @RequestBody ClienteEntrada item) {
        return hipermidia.recurso(servico.atualizar(id, ConversorDto.paraEntidade(item)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
