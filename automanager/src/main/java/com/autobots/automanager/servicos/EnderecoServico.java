package com.autobots.automanager.servicos;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
@Transactional
public class EnderecoServico {
    private final EnderecoRepositorio repositorio;
    private final ClienteRepositorio clientes;
    private final Validacao validacao;
    private final EnderecoAtualizador atualizador = new EnderecoAtualizador();
    public EnderecoServico(EnderecoRepositorio repositorio, ClienteRepositorio clientes, Validacao validacao) {
        this.repositorio = repositorio; this.clientes = clientes; this.validacao = validacao;
    }
    @Transactional(readOnly = true)
    public List<Endereco> listar() { return repositorio.findAll(); }
    @Transactional(readOnly = true)
    public Endereco buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereco nao encontrado"));
    }
    public Endereco criar(Endereco item, Long clienteId) {
        validacao.novo(item.getId()); validacao.validar(item);
        if (clienteId != null) {
            Cliente cliente = clientes.findById(clienteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
            if (cliente.getEndereco() != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente ja possui endereco");
            item = repositorio.save(item);
            cliente.setEndereco(item);
            clientes.saveAndFlush(cliente);
            return item;
        }
        return repositorio.saveAndFlush(item);
    }
    public Endereco atualizar(Long id, Endereco dados) {
        validacao.mesmoId(id, dados.getId());
        Endereco item = buscar(id); atualizador.atualizar(item, dados); validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public Endereco substituir(Long id, Endereco dados) {
        validacao.mesmoId(id, dados.getId());
        Endereco item = buscar(id);
        validacao.validar(dados);
        org.springframework.beans.BeanUtils.copyProperties(dados, item, "id", "links");
        validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public void excluir(Long id) {
        Endereco existente = buscar(id);
        var dono = clientes.findByEnderecoId(id);
        if (dono.isPresent()) {
            Cliente cliente = dono.get();
            cliente.setEndereco(null);
            clientes.saveAndFlush(cliente);
        } else { repositorio.delete(existente); repositorio.flush(); }
    }
}
