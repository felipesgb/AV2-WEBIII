package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/empresas", produces="application/hal+json")
public class EmpresaControle extends CrudBase<Empresa, EmpresaEntrada> {
 public EmpresaControle() { super(Empresa.class, "empresas"); }
 protected Set<String> filtrosPermitidos() { return Set.of("enderecoId"); }
 protected Empresa novo() { return new Empresa(); }
 protected void preencher(Empresa e, EmpresaEntrada d, boolean parcial) {
  if (!parcial || d.razaoSocial()!=null) e.setRazaoSocial(d.razaoSocial()==null ? null : d.razaoSocial());
  if (!parcial || d.nomeFantasia()!=null) e.setNomeFantasia(d.nomeFantasia()==null ? null : d.nomeFantasia());
  if (!parcial || d.cadastro()!=null) e.setCadastro(d.cadastro()==null ? LocalDate.now() : d.cadastro());
  if (!parcial || d.enderecoId()!=null) e.setEndereco(referencia(Endereco.class,d.enderecoId()));
  if (!parcial || d.telefonesIds()!=null) e.setTelefones(referencias(Telefone.class,d.telefonesIds()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Empresa e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("razaoSocial", e.getRazaoSocial());
  m.put("nomeFantasia", e.getNomeFantasia());
  m.put("cadastro", e.getCadastro());
  m.put("enderecoId", e.getEndereco()==null?null:e.getEndereco().getId());
  m.put("telefonesIds", e.getTelefones().stream().map(x->x.getId()).toList());
  return m;
 }
}
