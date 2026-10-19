package com.autobots.automanager.repositorios;

/** Projecao dos IDs necessarios para gerar links, sem carregar o agregado inteiro. */
public interface VinculoCliente {
    Long getRecursoId();
    Long getClienteId();
}
