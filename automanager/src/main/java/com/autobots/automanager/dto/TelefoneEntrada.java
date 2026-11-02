package com.autobots.automanager.dto;

import lombok.Data;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TelefoneEntrada {
    private Long id;
    private String ddd;
    private String numero;
}
