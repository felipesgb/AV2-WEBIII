package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/mercadorias", produces="application/hal+json")
public class MercadoriaControle extends CrudBase<Mercadoria, MercadoriaEntrada> {
 public MercadoriaControle() { super(Mercadoria.class, "mercadorias"); }
 protected Set<String> filtrosPermitidos() { return Set.of("empresaId","fornecedorId"); }
 protected Mercadoria novo() { return new Mercadoria(); }
 protected void preencher(Mercadoria e, MercadoriaEntrada d, boolean parcial) {
  if (!parcial || d.nome()!=null) e.setNome(d.nome()==null ? null : d.nome());
  if (!parcial || d.descricao()!=null) e.setDescricao(d.descricao()==null ? null : d.descricao());
  if (!parcial || d.valor()!=null) e.setValor(d.valor()==null ? null : d.valor());
  if (!parcial || d.quantidade()!=null) e.setQuantidade(d.quantidade()==null ? null : d.quantidade());
  if (!parcial || d.validade()!=null) e.setValidade(d.validade()==null ? null : d.validade());
  if (!parcial || d.fabricacao()!=null) e.setFabricacao(d.fabricacao()==null ? null : d.fabricacao());
  if (!parcial || d.cadastro()!=null) e.setCadastro(d.cadastro()==null ? LocalDate.now() : d.cadastro());
  if (!parcial || d.empresaId()!=null) e.setEmpresa(referencia(Empresa.class,d.empresaId()));
  if (!parcial || d.fornecedorId()!=null) e.setFornecedor(referencia(Usuario.class,d.fornecedorId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Mercadoria e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("nome", e.getNome());
  m.put("descricao", e.getDescricao());
  m.put("valor", e.getValor());
  m.put("quantidade", e.getQuantidade());
  m.put("validade", e.getValidade());
  m.put("fabricacao", e.getFabricacao());
  m.put("cadastro", e.getCadastro());
  m.put("empresaId", e.getEmpresa()==null?null:e.getEmpresa().getId());
  m.put("fornecedorId", e.getFornecedor()==null?null:e.getFornecedor().getId());
  return m;
 }
}
