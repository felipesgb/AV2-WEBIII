package com.autobots.automanager.servicos;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
@Transactional
public class TelefoneServico {
    private final TelefoneRepositorio repositorio;
    private final ClienteRepositorio clientes;
    private final Validacao validacao;
    private final TelefoneAtualizador atualizador = new TelefoneAtualizador();
    public TelefoneServico(TelefoneRepositorio repositorio, ClienteRepositorio clientes, Validacao validacao) {
        this.repositorio = repositorio; this.clientes = clientes; this.validacao = validacao;
    }
    @Transactional(readOnly = true)
    public List<Telefone> listar() { return repositorio.findAll(); }
    @Transactional(readOnly = true)
    public Telefone buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone nao encontrado"));
    }
    public Telefone criar(Telefone item, Long clienteId) {
        validacao.novo(item.getId()); validacao.validar(item);
        if (clienteId != null) {
            Cliente cliente = clientes.findById(clienteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
            item = repositorio.save(item);
            cliente.getTelefones().add(item);
            clientes.saveAndFlush(cliente);
            return item;
        }
        return repositorio.saveAndFlush(item);
    }
    public Telefone atualizar(Long id, Telefone dados) {
        validacao.mesmoId(id, dados.getId());
        Telefone item = buscar(id); atualizador.atualizar(item, dados); validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public Telefone substituir(Long id, Telefone dados) {
        validacao.mesmoId(id, dados.getId());
        Telefone item = buscar(id);
        validacao.validar(dados);
        org.springframework.beans.BeanUtils.copyProperties(dados, item, "id", "links");
        validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public void excluir(Long id) {
        Telefone existente = buscar(id);
        var dono = clientes.findByTelefonesId(id);
        if (dono.isPresent()) {
            Cliente cliente = dono.get();
            cliente.getTelefones().removeIf(item -> id.equals(item.getId()));
            clientes.saveAndFlush(cliente);
        } else { repositorio.delete(existente); repositorio.flush(); }
    }
}
