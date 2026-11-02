package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/usuarios", produces="application/hal+json")
public class UsuarioControle extends CrudBase<Usuario, UsuarioEntrada> {
 public UsuarioControle() { super(Usuario.class, "usuarios"); }
 protected Set<String> filtrosPermitidos() { return Set.of("empresaId","enderecoId"); }
 protected Usuario novo() { return new Usuario(); }
 protected void preencher(Usuario e, UsuarioEntrada d, boolean parcial) {
  if (!parcial || d.nome()!=null) e.setNome(d.nome()==null ? null : d.nome());
  if (!parcial || d.nomeSocial()!=null) e.setNomeSocial(d.nomeSocial()==null ? null : d.nomeSocial());
  if (!parcial || d.perfis()!=null) e.setPerfis(d.perfis()==null ? null : d.perfis());
  if (!parcial || d.empresaId()!=null) e.setEmpresa(referencia(Empresa.class,d.empresaId()));
  if (!parcial || d.enderecoId()!=null) e.setEndereco(referencia(Endereco.class,d.enderecoId()));
  if (!parcial || d.telefonesIds()!=null) e.setTelefones(referencias(Telefone.class,d.telefonesIds()));
  if (!parcial || d.documentosIds()!=null) e.setDocumentos(referencias(Documento.class,d.documentosIds()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Usuario e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("nome", e.getNome());
  m.put("nomeSocial", e.getNomeSocial());
  m.put("perfis", Set.copyOf(e.getPerfis()));
  m.put("empresaId", e.getEmpresa()==null?null:e.getEmpresa().getId());
  m.put("enderecoId", e.getEndereco()==null?null:e.getEndereco().getId());
  m.put("telefonesIds", e.getTelefones().stream().map(x->x.getId()).toList());
  m.put("documentosIds", e.getDocumentos().stream().map(x->x.getId()).toList());
  return m;
 }
}
