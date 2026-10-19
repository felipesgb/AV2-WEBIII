package com.autobots.automanager.modelo;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Telefone;

public class TelefoneAtualizador {
    public void atualizar(Telefone telefone, Telefone atualizacao) {
        if (atualizacao == null) return;
        if (atualizacao.getDdd() != null) { telefone.setDdd(atualizacao.getDdd()); }
        if (atualizacao.getNumero() != null) { telefone.setNumero(atualizacao.getNumero()); }
    }

    public void atualizar(List<Telefone> itens, List<Telefone> atualizacoes) {
        if (atualizacoes == null) return;
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (Telefone atualizacao : atualizacoes) {
            if (atualizacao == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item nulo");
            if (atualizacao.getId() == null) {
                itens.add(atualizacao);
            } else {
                if (!ids.add(atualizacao.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID repetido");
                Telefone existente = itens.stream().filter(i -> atualizacao.getId().equals(i.getId()))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item nao pertence ao cliente"));
                atualizar(existente, atualizacao);
            }
        }
    }

}
