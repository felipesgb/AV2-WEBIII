package com.autobots.automanager.dto;

import lombok.Data;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DocumentoEntrada {
    private Long id;
    private String tipo;
    private String numero;
    private java.time.LocalDate dataEmissao;
}
