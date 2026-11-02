package com.autobots.automanager.base;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
import java.time.LocalDate;
import org.springframework.hateoas.*;
@RestController
@RequestMapping(value="/vendas", produces="application/hal+json")
public class VendaControle extends CrudBase<Venda, VendaEntrada> {
 public VendaControle() { super(Venda.class, "vendas"); }
 protected Set<String> filtrosPermitidos() { return Set.of("empresaId","clienteId","funcionarioId","veiculoId"); }
 protected Venda novo() { return new Venda(); }
 protected void preencher(Venda e, VendaEntrada d, boolean parcial) {
  if (!parcial || d.identificacao()!=null) e.setIdentificacao(d.identificacao()==null ? null : d.identificacao());
  if (!parcial || d.cadastro()!=null) e.setCadastro(d.cadastro()==null ? LocalDate.now() : d.cadastro());
  if (!parcial || d.empresaId()!=null) e.setEmpresa(referencia(Empresa.class,d.empresaId()));
  if (!parcial || d.clienteId()!=null) e.setCliente(referencia(Usuario.class,d.clienteId()));
  if (!parcial || d.funcionarioId()!=null) e.setFuncionario(referencia(Usuario.class,d.funcionarioId()));
  if (!parcial || d.veiculoId()!=null) e.setVeiculo(referencia(Veiculo.class,d.veiculoId()));
  if (!parcial || d.mercadoriasIds()!=null) e.setMercadorias(referencias(Mercadoria.class,d.mercadoriasIds()));
  if (!parcial || d.servicosIds()!=null) e.setServicos(referencias(Servico.class,d.servicosIds()));
  Regras.validar(e);
 }
 protected Map<String,Object> dados(Venda e) {
  Map<String,Object> m = new LinkedHashMap<>(); m.put("id",e.getId());
  m.put("identificacao", e.getIdentificacao());
  m.put("cadastro", e.getCadastro());
  m.put("empresaId", e.getEmpresa()==null?null:e.getEmpresa().getId());
  m.put("clienteId", e.getCliente()==null?null:e.getCliente().getId());
  m.put("funcionarioId", e.getFuncionario()==null?null:e.getFuncionario().getId());
  m.put("veiculoId", e.getVeiculo()==null?null:e.getVeiculo().getId());
  m.put("mercadoriasIds", e.getMercadorias().stream().map(x->x.getId()).toList());
  m.put("servicosIds", e.getServicos().stream().map(x->x.getId()).toList());
  return m;
 }
}
