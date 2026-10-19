package com.autobots.automanager.modelo;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Documento;

public class DocumentoAtualizador {
    public void atualizar(Documento documento, Documento atualizacao) {
        if (atualizacao == null) return;
        if (atualizacao.getTipo() != null) { documento.setTipo(atualizacao.getTipo()); }
        if (atualizacao.getNumero() != null) { documento.setNumero(atualizacao.getNumero()); }
    }

    public void atualizar(List<Documento> itens, List<Documento> atualizacoes) {
        if (atualizacoes == null) return;
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (Documento atualizacao : atualizacoes) {
            if (atualizacao == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item nulo");
            if (atualizacao.getId() == null) {
                itens.add(atualizacao);
            } else {
                if (!ids.add(atualizacao.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID repetido");
                Documento existente = itens.stream().filter(i -> atualizacao.getId().equals(i.getId()))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item nao pertence ao cliente"));
                atualizar(existente, atualizacao);
            }
        }
    }

}
