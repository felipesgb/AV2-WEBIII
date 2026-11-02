package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/credenciais-senha", produces="application/hal+json")
public class CredencialUsuarioSenhaControle extends CrudBase<CredencialUsuarioSenha, CredencialUsuarioSenhaEntrada> {
 public CredencialUsuarioSenhaControle() { super(CredencialUsuarioSenha.class, "credenciais-senha"); }
 protected Set<String> filtrosPermitidos() { return Set.of("usuarioId"); }
 protected CredencialUsuarioSenha novo() { return new CredencialUsuarioSenha(); }
 protected void preencher(CredencialUsuarioSenha e, CredencialUsuarioSenhaEntrada d, boolean parcial) {
  if (!parcial || d.nomeUsuario()!=null) e.setNomeUsuario(d.nomeUsuario()==null ? null : d.nomeUsuario());
  if (!parcial || d.senha()!=null) e.setSenha(d.senha()==null ? null : Senhas.hash(d.senha()));
  if (!parcial || d.criacao()!=null) e.setCriacao(d.criacao()==null ? LocalDate.now() : d.criacao());
  if (!parcial || d.ultimoAcesso()!=null) e.setUltimoAcesso(d.ultimoAcesso()==null ? null : d.ultimoAcesso());
  if (!parcial || d.inativo()!=null) e.setInativo(d.inativo()==null ? false : d.inativo());
  if (!parcial || d.usuarioId()!=null) e.setUsuario(referencia(Usuario.class,d.usuarioId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(CredencialUsuarioSenha e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("nomeUsuario", e.getNomeUsuario());
  m.put("criacao", e.getCriacao());
  m.put("ultimoAcesso", e.getUltimoAcesso());
  m.put("inativo", e.getInativo());
  m.put("usuarioId", e.getUsuario()==null?null:e.getUsuario().getId());
  return m;
 }
}
