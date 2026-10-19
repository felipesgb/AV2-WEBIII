package com.autobots.automanager.dto;

import lombok.Data;

@Data
public class EnderecoEntrada {
    private Long id;
    private String estado;
    private String cidade;
    private String bairro;
    private String rua;
    private String numero;
    private String codigoPostal;
    private String informacoesAdicionais;
}
