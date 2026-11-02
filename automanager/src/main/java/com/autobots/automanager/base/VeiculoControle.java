package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/veiculos", produces="application/hal+json")
public class VeiculoControle extends CrudBase<Veiculo, VeiculoEntrada> {
 public VeiculoControle() { super(Veiculo.class, "veiculos"); }
 protected Set<String> filtrosPermitidos() { return Set.of("proprietarioId"); }
 protected Veiculo novo() { return new Veiculo(); }
 protected void preencher(Veiculo e, VeiculoEntrada d, boolean parcial) {
  if (!parcial || d.tipo()!=null) e.setTipo(d.tipo()==null ? null : d.tipo());
  if (!parcial || d.modelo()!=null) e.setModelo(d.modelo()==null ? null : d.modelo());
  if (!parcial || d.placa()!=null) e.setPlaca(d.placa()==null ? null : d.placa().trim().toUpperCase(Locale.ROOT).replace("-", ""));
  if (!parcial || d.proprietarioId()!=null) e.setProprietario(referencia(Usuario.class,d.proprietarioId()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Veiculo e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("tipo", e.getTipo());
  m.put("modelo", e.getModelo());
  m.put("placa", e.getPlaca());
  m.put("proprietarioId", e.getProprietario()==null?null:e.getProprietario().getId());
  return m;
 }
}
