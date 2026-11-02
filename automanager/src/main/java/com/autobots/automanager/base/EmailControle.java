package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/emails", produces="application/hal+json")
public class EmailControle extends CrudBase<Email, EmailEntrada> {
 public EmailControle() { super(Email.class, "emails"); }
 protected Set<String> filtrosPermitidos() { return Set.of("usuarioId"); }
 protected Email novo() { return new Email(); }
 protected void preencher(Email e, EmailEntrada d, boolean parcial) {
  if (!parcial || d.endereco()!=null) e.setEndereco(d.endereco()==null ? null : d.endereco());
  if (!parcial || d.usuarioId()!=null) e.setUsuario(referencia(Usuario.class,d.usuarioId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Email e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("endereco", e.getEndereco());
  m.put("usuarioId", e.getUsuario()==null?null:e.getUsuario().getId());
  return m;
 }
}
