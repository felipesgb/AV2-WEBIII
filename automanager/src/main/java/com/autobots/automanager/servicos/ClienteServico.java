package com.autobots.automanager.servicos;
import java.util.List;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
@Transactional
public class ClienteServico {
    private final ClienteRepositorio repositorio;
    private final Validacao validacao;
    private final ClienteAtualizador atualizador = new ClienteAtualizador();
    public ClienteServico(ClienteRepositorio repositorio, Validacao validacao) {
        this.repositorio = repositorio; this.validacao = validacao;
    }
    // Inicializa as colecoes dentro da transacao, sem depender de Open Session in View.
    private Cliente carregar(Cliente cliente) {
        cliente.getDocumentos().size(); cliente.getTelefones().size();
        return cliente;
    }
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        List<Cliente> clientes = repositorio.findAll(); clientes.forEach(this::carregar); return clientes;
    }
    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return carregar(repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado")));
    }
    public Cliente criar(Cliente cliente) {
        validacao.novo(cliente.getId());
        validacao.validar(cliente);
        if (cliente.getEndereco() != null) validacao.novo(cliente.getEndereco().getId());
        cliente.getDocumentos().forEach(d -> validacao.novo(d.getId()));
        cliente.getTelefones().forEach(t -> validacao.novo(t.getId()));
        if (cliente.getDataCadastro() == null) cliente.setDataCadastro(new Date());
        return repositorio.saveAndFlush(cliente);
    }
    public Cliente atualizar(Long id, Cliente dados) {
        validacao.mesmoId(id, dados.getId());
        Cliente cliente = buscar(id);
        atualizador.atualizar(cliente, dados);
        validacao.validar(cliente);
        return repositorio.saveAndFlush(cliente);
    }
    public Cliente substituir(Long id, Cliente dados) {
        validacao.mesmoId(id, dados.getId());
        Cliente item = buscar(id);
        validacao.validar(dados);
        com.autobots.automanager.modelo.ClienteSubstituidor.substituir(item, dados);
        validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public void excluir(Long id) { repositorio.delete(buscar(id)); repositorio.flush(); }
}
