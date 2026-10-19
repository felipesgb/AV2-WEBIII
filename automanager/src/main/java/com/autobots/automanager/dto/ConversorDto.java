package com.autobots.automanager.dto;

import com.autobots.automanager.entidades.*;
import java.util.ArrayList;

/** Conversao explicita: o contrato HTTP nao expoe nem recebe entidades persistentes. */
public final class ConversorDto {
    private ConversorDto() { }
    public static Cliente paraEntidade(ClienteEntrada dados) {
        if (dados == null) return null;
        Cliente entidade = new Cliente();
        entidade.setId(dados.getId());
        entidade.setNome(dados.getNome());
        entidade.setNomeSocial(dados.getNomeSocial());
        entidade.setDataNascimento(dados.getDataNascimento());
        entidade.setDataCadastro(dados.getDataCadastro());
        entidade.setEndereco(paraEntidade(dados.getEndereco()));
        entidade.setDocumentos(dados.getDocumentos() == null ? null : new ArrayList<>(
            dados.getDocumentos().stream().map(ConversorDto::paraEntidade).toList()));
        entidade.setTelefones(dados.getTelefones() == null ? null : new ArrayList<>(
            dados.getTelefones().stream().map(ConversorDto::paraEntidade).toList()));
        return entidade;
    }

    public static ClienteSaida paraSaida(Cliente entidade) {
        ClienteSaida dados = new ClienteSaida();
        dados.setId(entidade.getId());
        dados.setNome(entidade.getNome());
        dados.setNomeSocial(entidade.getNomeSocial());
        dados.setDataNascimento(entidade.getDataNascimento());
        dados.setDataCadastro(entidade.getDataCadastro());
        return dados;
    }

    public static Documento paraEntidade(DocumentoEntrada dados) {
        if (dados == null) return null;
        Documento entidade = new Documento();
        entidade.setId(dados.getId());
        entidade.setTipo(dados.getTipo());
        entidade.setNumero(dados.getNumero());
        return entidade;
    }

    public static DocumentoSaida paraSaida(Documento entidade) {
        DocumentoSaida dados = new DocumentoSaida();
        dados.setId(entidade.getId());
        dados.setTipo(entidade.getTipo());
        dados.setNumero(entidade.getNumero());
        return dados;
    }

    public static Telefone paraEntidade(TelefoneEntrada dados) {
        if (dados == null) return null;
        Telefone entidade = new Telefone();
        entidade.setId(dados.getId());
        entidade.setDdd(dados.getDdd());
        entidade.setNumero(dados.getNumero());
        return entidade;
    }

    public static TelefoneSaida paraSaida(Telefone entidade) {
        TelefoneSaida dados = new TelefoneSaida();
        dados.setId(entidade.getId());
        dados.setDdd(entidade.getDdd());
        dados.setNumero(entidade.getNumero());
        return dados;
    }

    public static Endereco paraEntidade(EnderecoEntrada dados) {
        if (dados == null) return null;
        Endereco entidade = new Endereco();
        entidade.setId(dados.getId());
        entidade.setEstado(dados.getEstado());
        entidade.setCidade(dados.getCidade());
        entidade.setBairro(dados.getBairro());
        entidade.setRua(dados.getRua());
        entidade.setNumero(dados.getNumero());
        entidade.setCodigoPostal(dados.getCodigoPostal());
        entidade.setInformacoesAdicionais(dados.getInformacoesAdicionais());
        return entidade;
    }

    public static EnderecoSaida paraSaida(Endereco entidade) {
        EnderecoSaida dados = new EnderecoSaida();
        dados.setId(entidade.getId());
        dados.setEstado(entidade.getEstado());
        dados.setCidade(entidade.getCidade());
        dados.setBairro(entidade.getBairro());
        dados.setRua(entidade.getRua());
        dados.setNumero(entidade.getNumero());
        dados.setCodigoPostal(entidade.getCodigoPostal());
        dados.setInformacoesAdicionais(entidade.getInformacoesAdicionais());
        return dados;
    }

}
