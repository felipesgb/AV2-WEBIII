package com.autobots.automanager.base;

import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.hateoas.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** CRUD comum: recebe DTOs, resolve referencias existentes e nunca salva grafos enviados pelo cliente. */
@Transactional
public abstract class CrudBase<E extends Registro,D> {
    @PersistenceContext protected EntityManager em;
    @Autowired private Validator validator;
    private final Class<E> tipo;
    private final String rota;
    protected CrudBase(Class<E> tipo, String rota) { this.tipo=tipo; this.rota=rota; }
    protected abstract E novo();
    protected abstract void preencher(E e,D d,boolean parcial);
    protected abstract Map<String,Object> dados(E e);
    protected abstract Set<String> filtrosPermitidos();
    protected <T> T referencia(Class<T> classe,Long id) {
        if(id==null) return null;
        T e=em.find(classe,id);
        if(e==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,classe.getSimpleName()+" nao encontrado: "+id);
        return e;
    }
    protected <T> Set<T> referencias(Class<T> classe,Set<Long> ids) {
        Set<T> r=new LinkedHashSet<>();
        if(ids!=null) for(Long id:ids) {
            if(id==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID nulo na lista");
            r.add(referencia(classe,id));
        }
        return r;
    }
    private E buscar(Long id) { return referencia(tipo,id); }
    private String url(String caminho) { return ServletUriComponentsBuilder.fromCurrentContextPath().path(caminho).build().toUriString(); }
    private EntityModel<Map<String,Object>> modelo(E e) {
        var d=dados(e);
        var m=EntityModel.of(d);
        String item=url("/"+rota+"/"+e.getId());
        m.add(Link.of(item).withSelfRel(),Link.of(url("/"+rota)).withRel("collection"),
            Link.of(item).withRel("atualizar"),Link.of(item).withRel("excluir"),Link.of(url("/")).withRel("inicio"));
        Map<String,String> destinos=Map.ofEntries(
            Map.entry("empresa","empresas"),Map.entry("usuario","usuarios"),Map.entry("proprietario","usuarios"),
            Map.entry("cliente","usuarios"),Map.entry("funcionario","usuarios"),Map.entry("fornecedor","usuarios"),
            Map.entry("veiculo","veiculos"),Map.entry("endereco","enderecos"),Map.entry("telefones","telefones"),
            Map.entry("documentos","documentos"),Map.entry("mercadorias","mercadorias"),Map.entry("servicos","servicos"));
        destinos.forEach((rel,r)-> {
            Object id=d.get(rel+"Id");
            if(id!=null) m.add(Link.of(url("/"+r+"/"+id)).withRel(rel));
            Object ids=d.get(rel+"Ids");
            if(ids instanceof Collection<?> lista) for(Object x:lista) m.add(Link.of(url("/"+r+"/"+x)).withRel(rel));
        });
        if(e instanceof Empresa) for(String r:List.of("usuarios","mercadorias","servicos","vendas"))
            m.add(Link.of(url("/"+r)+"?empresaId="+e.getId()).withRel(r));
        if(e instanceof Usuario) {
            m.add(Link.of(url("/veiculos")+"?proprietarioId="+e.getId()).withRel("veiculos"));
            for(String r:List.of("emails","credenciais-senha","credenciais-codigo")) m.add(Link.of(url("/"+r)+"?usuarioId="+e.getId()).withRel(r));
            m.add(Link.of(url("/vendas")+"?clienteId="+e.getId()).withRel("vendas"));
            m.add(Link.of(url("/mercadorias")+"?fornecedorId="+e.getId()).withRel("mercadorias"));
        }
        if(e instanceof Veiculo) m.add(Link.of(url("/vendas")+"?veiculoId="+e.getId()).withRel("vendas"));
        return m;
    }
    @GetMapping @Transactional(readOnly=true)
    public CollectionModel<EntityModel<Map<String,Object>>> listar(@RequestParam Map<String,String> filtros) {
        var itens=em.createQuery("select e from "+tipo.getSimpleName()+" e order by e.id",tipo).getResultList();
        for(String chave:filtros.keySet()) if(!filtrosPermitidos().contains(chave) || !filtros.get(chave).matches("[1-9][0-9]*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Filtro invalido: "+chave);
        var modelos=itens.stream().filter(e->filtros.entrySet().stream().allMatch(f->Objects.toString(dados(e).get(f.getKey()),"").equals(f.getValue()))).map(this::modelo).toList();
        String atual=ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
        return CollectionModel.of(modelos,Link.of(atual).withSelfRel(),Link.of(url("/"+rota)).withRel("cadastrar"),Link.of(url("/")).withRel("inicio"));
    }
    @GetMapping("/{id}") @Transactional(readOnly=true)
    public EntityModel<Map<String,Object>> obter(@PathVariable Long id) { return modelo(buscar(id)); }
    private void validar(E e) {
        var erros=validator.validate(e);
        if(!erros.isEmpty()) throw new ConstraintViolationException(erros);
    }
    @PostMapping(consumes="application/json")
    public ResponseEntity<?> criar(@RequestBody D d) {
        E e=novo(); preencher(e,d,false); validar(e); em.persist(e); em.flush();
        var m=modelo(e); return ResponseEntity.created(java.net.URI.create(m.getRequiredLink("self").getHref())).body(m);
    }
    @PutMapping(value="/{id}",consumes="application/json")
    public EntityModel<Map<String,Object>> substituir(@PathVariable Long id,@RequestBody D d) { return alterar(id,d,false); }
    @PatchMapping(value="/{id}",consumes="application/json")
    public EntityModel<Map<String,Object>> atualizar(@PathVariable Long id,@RequestBody D d) { return alterar(id,d,true); }
    private EntityModel<Map<String,Object>> alterar(Long id,D d,boolean parcial) {
        E e=buscar(id); preencher(e,d,parcial); validar(e);
        // Alteracoes de perfil, empresa ou proprietario nao podem invalidar registros vinculados.
        for(Class<?> dependente:List.of(Veiculo.class,Mercadoria.class,Venda.class)) {
            var registros=em.createQuery("select e from "+dependente.getSimpleName()+" e",dependente)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT).getResultList();
            registros.forEach(Regras::validar);
        }
        em.flush(); return modelo(e);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) { em.remove(buscar(id)); em.flush(); return ResponseEntity.noContent().build(); }
}
