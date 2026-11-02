package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/credenciais-codigo", produces="application/hal+json")
public class CredencialCodigoBarraControle extends CrudBase<CredencialCodigoBarra, CredencialCodigoBarraEntrada> {
 public CredencialCodigoBarraControle() { super(CredencialCodigoBarra.class, "credenciais-codigo"); }
 protected Set<String> filtrosPermitidos() { return Set.of("usuarioId"); }
 protected CredencialCodigoBarra novo() { return new CredencialCodigoBarra(); }
 protected void preencher(CredencialCodigoBarra e, CredencialCodigoBarraEntrada d, boolean parcial) {
  if (!parcial || d.codigo()!=null) e.setCodigo(d.codigo()==null ? null : d.codigo());
  if (!parcial || d.criacao()!=null) e.setCriacao(d.criacao()==null ? LocalDate.now() : d.criacao());
  if (!parcial || d.ultimoAcesso()!=null) e.setUltimoAcesso(d.ultimoAcesso()==null ? null : d.ultimoAcesso());
  if (!parcial || d.inativo()!=null) e.setInativo(d.inativo()==null ? false : d.inativo());
  if (!parcial || d.usuarioId()!=null) e.setUsuario(referencia(Usuario.class,d.usuarioId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(CredencialCodigoBarra e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("codigo", e.getCodigo());
  m.put("criacao", e.getCriacao());
  m.put("ultimoAcesso", e.getUltimoAcesso());
  m.put("inativo", e.getInativo());
  m.put("usuarioId", e.getUsuario()==null?null:e.getUsuario().getId());
  return m;
 }
}
