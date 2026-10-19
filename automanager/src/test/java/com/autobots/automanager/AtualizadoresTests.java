package com.autobots.automanager;

import java.util.ArrayList;
import java.util.List;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.modelo.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AtualizadoresTests {
    @Test void comparaIdsPeloValorAcimaDoCacheLong() {
        Documento original = new Documento(); original.setId(Long.valueOf("1000")); original.setNumero("A");
        Documento dados = new Documento(); dados.setId(Long.valueOf("1000")); dados.setNumero("B");
        new DocumentoAtualizador().atualizar(new ArrayList<>(List.of(original)), List.of(dados));
        assertThat(original.getNumero()).isEqualTo("B");
        Telefone telefone = new Telefone(); telefone.setId(Long.valueOf("1000")); telefone.setDdd("11");
        Telefone atualizacao = new Telefone(); atualizacao.setId(Long.valueOf("1000")); atualizacao.setDdd("21");
        new TelefoneAtualizador().atualizar(new ArrayList<>(List.of(telefone)), List.of(atualizacao));
        assertThat(telefone.getDdd()).isEqualTo("21");
    }
    @Test void listasNulasNaoCausamFalha() {
        assertThatCode(() -> new DocumentoAtualizador().atualizar(new ArrayList<>(), null)).doesNotThrowAnyException();
        assertThatCode(() -> new TelefoneAtualizador().atualizar(new ArrayList<>(), null)).doesNotThrowAnyException();
    }
    @Test void rejeitaIdRepetidoEmAtualizacao() {
        Documento d = new Documento(); d.setId(1000L);
        assertThatThrownBy(() -> new DocumentoAtualizador().atualizar(new ArrayList<>(List.of(d)), List.of(d, d)))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
}
