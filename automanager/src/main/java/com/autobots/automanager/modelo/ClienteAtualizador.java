package com.autobots.automanager.modelo;
import com.autobots.automanager.entidades.Cliente;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ClienteAtualizador {
    private final EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
    private final DocumentoAtualizador documentoAtualizador = new DocumentoAtualizador();
    private final TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();

    public void atualizar(Cliente cliente, Cliente atualizacao) {
        if (atualizacao.getNome() != null) cliente.setNome(atualizacao.getNome());
        if (atualizacao.getNomeSocial() != null) cliente.setNomeSocial(atualizacao.getNomeSocial());
        if (atualizacao.getDataNascimento() != null) cliente.setDataNascimento(atualizacao.getDataNascimento());
        if (atualizacao.getDataCadastro() != null) cliente.setDataCadastro(atualizacao.getDataCadastro());
        if (atualizacao.getEndereco() != null) {
            Long id = atualizacao.getEndereco().getId();
            if (id != null && (cliente.getEndereco() == null || !id.equals(cliente.getEndereco().getId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereco nao pertence ao cliente");
            }
            if (cliente.getEndereco() == null) cliente.setEndereco(atualizacao.getEndereco());
            else enderecoAtualizador.atualizar(cliente.getEndereco(), atualizacao.getEndereco());
        }
        documentoAtualizador.atualizar(cliente.getDocumentos(), atualizacao.getDocumentos());
        telefoneAtualizador.atualizar(cliente.getTelefones(), atualizacao.getTelefones());
    }
}
