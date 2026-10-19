package com.autobots.automanager.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Data
public class ClienteEntrada {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Date dataNascimento;
    private Date dataCadastro;
    private List<DocumentoEntrada> documentos = new ArrayList<>();
    private EnderecoEntrada endereco;
    private List<TelefoneEntrada> telefones = new ArrayList<>();
}
