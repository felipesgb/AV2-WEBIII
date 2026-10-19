package com.autobots.automanager.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.*;

/** Substituicao completa dos campos editaveis, preservando identidade e data de cadastro. */
public final class ClienteSubstituidor {
    private ClienteSubstituidor() { }

    public static void substituir(Cliente atual, Cliente dados) {
        BeanUtils.copyProperties(dados, atual, "id", "links", "dataCadastro", "endereco", "documentos", "telefones");
        substituirLista(atual.getDocumentos(), dados.getDocumentos(), Documento::getId, Documento::getNumero);
        substituirLista(atual.getTelefones(), dados.getTelefones(), Telefone::getId, t -> t.getDdd() + ":" + t.getNumero());
        Endereco recebido = dados.getEndereco();
        if (recebido == null) {
            atual.setEndereco(null);
        } else if (atual.getEndereco() == null) {
            if (recebido.getId() != null) throw invalido("Endereco nao pertence ao cliente");
            atual.setEndereco(recebido);
        } else {
            if (recebido.getId() != null && !recebido.getId().equals(atual.getEndereco().getId())) {
                throw invalido("Endereco nao pertence ao cliente");
            }
            BeanUtils.copyProperties(recebido, atual.getEndereco(), "id", "links");
        }
    }

    private static <T> void substituirLista(List<T> atuais, List<T> recebidos,
            Function<T, Long> id, Function<T, String> chave) {
        List<T> resultado = new ArrayList<>();
        Set<String> chaves = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        for (T recebido : recebidos) {
            Long identificador = id.apply(recebido);
            if (!chaves.add(chave.apply(recebido)) || (identificador != null && !ids.add(identificador))) {
                throw invalido("Registro repetido na colecao");
            }
            T destino;
            if (identificador != null) {
                destino = atuais.stream().filter(a -> identificador.equals(id.apply(a))).findFirst()
                    .orElseThrow(() -> invalido("Registro nao pertence ao cliente"));
            } else {
                // Repetir o mesmo PUT nao cria novos filhos: reutiliza a identidade pela chave natural.
                destino = atuais.stream().filter(a -> chave.apply(recebido).equals(chave.apply(a)))
                    .findFirst().orElse(recebido);
            }
            if (resultado.contains(destino)) throw invalido("Registro repetido na colecao");
            if (destino != recebido) BeanUtils.copyProperties(recebido, destino, "id", "links");
            resultado.add(destino);
        }
        // Mantem a colecao gerenciada para o orphanRemoval excluir somente os filhos removidos.
        atuais.clear();
        atuais.addAll(resultado);
    }

    private static ResponseStatusException invalido(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
