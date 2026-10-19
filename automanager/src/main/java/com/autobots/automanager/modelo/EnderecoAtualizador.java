package com.autobots.automanager.modelo;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Endereco;

public class EnderecoAtualizador {
    public void atualizar(Endereco endereco, Endereco atualizacao) {
        if (atualizacao == null) return;
        if (atualizacao.getEstado() != null) { endereco.setEstado(atualizacao.getEstado()); }
        if (atualizacao.getCidade() != null) { endereco.setCidade(atualizacao.getCidade()); }
        if (atualizacao.getBairro() != null) { endereco.setBairro(atualizacao.getBairro()); }
        if (atualizacao.getRua() != null) { endereco.setRua(atualizacao.getRua()); }
        if (atualizacao.getNumero() != null) { endereco.setNumero(atualizacao.getNumero()); }
        if (atualizacao.getCodigoPostal() != null) { endereco.setCodigoPostal(atualizacao.getCodigoPostal()); }
        if (atualizacao.getInformacoesAdicionais() != null) { endereco.setInformacoesAdicionais(atualizacao.getInformacoesAdicionais()); }
    }

}
