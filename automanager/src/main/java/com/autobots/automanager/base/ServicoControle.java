package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/servicos", produces="application/hal+json")
public class ServicoControle extends CrudBase<Servico, ServicoEntrada> {
 public ServicoControle() { super(Servico.class, "servicos"); }
 protected Set<String> filtrosPermitidos() { return Set.of("empresaId"); }
 protected Servico novo() { return new Servico(); }
 protected void preencher(Servico e, ServicoEntrada d, boolean parcial) {
  if (!parcial || d.nome()!=null) e.setNome(d.nome()==null ? null : d.nome());
  if (!parcial || d.descricao()!=null) e.setDescricao(d.descricao()==null ? null : d.descricao());
  if (!parcial || d.valor()!=null) e.setValor(d.valor()==null ? null : d.valor());
  if (!parcial || d.empresaId()!=null) e.setEmpresa(referencia(Empresa.class,d.empresaId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Servico e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("nome", e.getNome());
  m.put("descricao", e.getDescricao());
  m.put("valor", e.getValor());
  m.put("empresaId", e.getEmpresa()==null?null:e.getEmpresa().getId());
  return m;
 }
}
