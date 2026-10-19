package com.autobots.automanager.servicos;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
@Transactional
public class DocumentoServico {
    private final DocumentoRepositorio repositorio;
    private final ClienteRepositorio clientes;
    private final Validacao validacao;
    private final DocumentoAtualizador atualizador = new DocumentoAtualizador();
    public DocumentoServico(DocumentoRepositorio repositorio, ClienteRepositorio clientes, Validacao validacao) {
        this.repositorio = repositorio; this.clientes = clientes; this.validacao = validacao;
    }
    @Transactional(readOnly = true)
    public List<Documento> listar() { return repositorio.findAll(); }
    @Transactional(readOnly = true)
    public Documento buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento nao encontrado"));
    }
    public Documento criar(Documento item, Long clienteId) {
        validacao.novo(item.getId()); validacao.validar(item);
        if (clienteId != null) {
            Cliente cliente = clientes.findById(clienteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
            item = repositorio.save(item);
            cliente.getDocumentos().add(item);
            clientes.saveAndFlush(cliente);
            return item;
        }
        return repositorio.saveAndFlush(item);
    }
    public Documento atualizar(Long id, Documento dados) {
        validacao.mesmoId(id, dados.getId());
        Documento item = buscar(id); atualizador.atualizar(item, dados); validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public Documento substituir(Long id, Documento dados) {
        validacao.mesmoId(id, dados.getId());
        Documento item = buscar(id);
        validacao.validar(dados);
        org.springframework.beans.BeanUtils.copyProperties(dados, item, "id", "links");
        validacao.validar(item);
        return repositorio.saveAndFlush(item);
    }
    public void excluir(Long id) {
        Documento existente = buscar(id);
        var dono = clientes.findByDocumentosId(id);
        if (dono.isPresent()) {
            Cliente cliente = dono.get();
            cliente.getDocumentos().removeIf(item -> id.equals(item.getId()));
            clientes.saveAndFlush(cliente);
        } else { repositorio.delete(existente); repositorio.flush(); }
    }
}
