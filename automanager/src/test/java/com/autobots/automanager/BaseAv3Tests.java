package com.autobots.automanager;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class BaseAv3Tests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 long empresa,cliente,funcionario,veiculo,servico;
 JsonNode criar(String rota,String corpo) throws Exception {
  var r=mvc.perform(post("/"+rota).contentType("application/json").content(corpo)).andExpect(status().isCreated()).andReturn().getResponse();
  var n=json.readTree(r.getContentAsString());
  assertThat(r.getHeader("Location")).isEqualTo(n.path("_links").path("self").path("href").asText()); return n;
 }
 @BeforeEach void base() throws Exception {
  empresa=criar("empresas","{\"razaoSocial\":\"Oficina\"}").path("id").asLong();
  cliente=criar("usuarios","{\"nome\":\"Ana\",\"perfis\":[\"CLIENTE\"],\"empresaId\":"+empresa+"}").path("id").asLong();
  funcionario=criar("usuarios","{\"nome\":\"Bia\",\"perfis\":[\"FUNCIONARIO\"],\"empresaId\":"+empresa+"}").path("id").asLong();
  veiculo=criar("veiculos","{\"modelo\":\"Gol\",\"placa\":\"ABC1D23\",\"tipo\":\"HATCH\",\"proprietarioId\":"+cliente+"}").path("id").asLong();
  servico=criar("servicos","{\"nome\":\"Revisao\",\"valor\":100,\"empresaId\":"+empresa+"}").path("id").asLong();
 }
 String venda() { return "{\"identificacao\":\"VENDA-01\",\"empresaId\":"+empresa+",\"clienteId\":"+cliente+",\"funcionarioId\":"+funcionario+",\"veiculoId\":"+veiculo+",\"servicosIds\":["+servico+"]}"; }
 @ParameterizedTest @ValueSource(strings={"empresas","usuarios","veiculos","mercadorias","servicos","vendas","emails","credenciais-senha","credenciais-codigo"})
 void crudComNavegacao(String rota) throws Exception {
  String corpo=switch(rota) {
   case "empresas" -> "{\"razaoSocial\":\"Outra\"}";
   case "usuarios" -> "{\"nome\":\"Fornecedor\",\"perfis\":[\"FORNECEDOR\"],\"empresaId\":"+empresa+"}";
   case "veiculos" -> "{\"modelo\":\"Polo\",\"placa\":\"XYZ1D23\",\"tipo\":\"HATCH\",\"proprietarioId\":"+cliente+"}";
   case "mercadorias" -> "{\"nome\":\"Filtro\",\"valor\":30.50,\"quantidade\":5,\"fabricacao\":\"2026-01-01\",\"validade\":\"2030-01-01\",\"empresaId\":"+empresa+"}";
   case "servicos" -> "{\"nome\":\"Troca\",\"valor\":50,\"empresaId\":"+empresa+"}";
   case "vendas" -> venda();
   case "emails" -> "{\"endereco\":\"ana@example.com\",\"usuarioId\":"+cliente+"}";
   case "credenciais-senha" -> "{\"nomeUsuario\":\"ana\",\"senha\":\"segredo\",\"usuarioId\":"+cliente+"}";
   default -> "{\"codigo\":123456,\"usuarioId\":"+cliente+"}";
  };
  var n=criar(rota,corpo); String item="/"+rota+"/"+n.path("id").asLong();
  mvc.perform(get("/")).andExpect(jsonPath("$._links['"+rota+"'].href").exists());
  mvc.perform(get("/"+rota)).andExpect(status().isOk()).andExpect(jsonPath("$._links.cadastrar.href").exists());
  mvc.perform(get(item)).andExpect(status().isOk()).andExpect(jsonPath("$._links.self.href").exists());
  mvc.perform(patch(item).contentType("application/json").content("{}")).andExpect(status().isOk());
  mvc.perform(put(item).contentType("application/json").content(corpo)).andExpect(status().isOk());
  if(rota.equals("credenciais-senha")) {
   assertThat(n.has("senha")).isFalse(); assertThat(n.toString()).doesNotContain("segredo","pbkdf2");
  }
  mvc.perform(delete(item)).andExpect(status().isNoContent());
  mvc.perform(get(item)).andExpect(status().isNotFound());
 }
 @Test void vendaRejeitaFuncionarioComPerfilCliente() throws Exception {
  mvc.perform(post("/vendas").contentType("application/json").content(venda().replace("\"funcionarioId\":"+funcionario,"\"funcionarioId\":"+cliente))).andExpect(status().isBadRequest());
 }
 @Test void vendaRejeitaServicoDeOutraEmpresa() throws Exception {
  long outra=criar("empresas","{\"razaoSocial\":\"Outra\"}").path("id").asLong();
  long outro=criar("servicos","{\"nome\":\"Outro\",\"valor\":10,\"empresaId\":"+outra+"}").path("id").asLong();
  mvc.perform(post("/vendas").contentType("application/json").content(venda().replace("["+servico+"]","["+outro+"]"))).andExpect(status().isBadRequest());
 }
 @Test void filtraUsuariosDaEmpresa() throws Exception {
  mvc.perform(get("/usuarios").param("empresaId",String.valueOf(empresa))).andExpect(status().isOk());
  var n=json.readTree(mvc.perform(get("/usuarios").param("empresaId","999999")).andReturn().getResponse().getContentAsString());
  assertThat(n.has("_embedded")).isFalse();
 }
 @Test void camposObrigatoriosEReferenciasAusentes() throws Exception {
  mvc.perform(post("/empresas").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  mvc.perform(post("/usuarios").contentType("application/json").content("{\"nome\":\"Ana\",\"perfis\":[\"CLIENTE\"],\"empresaId\":999999}")).andExpect(status().isNotFound());
  mvc.perform(post("/veiculos").contentType("application/json").content("{\"tipo\":\"INVALIDO\"}")).andExpect(status().isBadRequest());
 }
 @Test void perfilNaoPodeInvalidarVeiculoExistente() throws Exception {
  mvc.perform(patch("/usuarios/"+cliente).contentType("application/json").content("{\"perfis\":[\"FUNCIONARIO\"]}")).andExpect(status().isBadRequest());
 }
 @Test void normalizaPlacaERejeitaFormatoInvalido() throws Exception {
  mvc.perform(patch("/veiculos/"+veiculo).contentType("application/json").content("{\"placa\":\" xyz-1234 \"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.placa").value("XYZ1234"));
  mvc.perform(patch("/veiculos/"+veiculo).contentType("application/json").content("{\"placa\":\"qualquercoisa\"}"))
   .andExpect(status().isBadRequest());
 }
 @Test void servicoNaoPodeMudarDeEmpresaDepoisDaVenda() throws Exception {
  criar("vendas",venda());
  long outra=criar("empresas","{\"razaoSocial\":\"Outra\"}").path("id").asLong();
  mvc.perform(patch("/servicos/"+servico).contentType("application/json").content("{\"empresaId\":"+outra+"}")).andExpect(status().isBadRequest());
 }
 @Test void rejeitaCampoDesconhecidoEEmailInvalido() throws Exception {
  mvc.perform(post("/empresas").contentType("application/json").content("{\"id\":123,\"razaoSocial\":\"Outra\"}")).andExpect(status().isBadRequest());
  mvc.perform(post("/emails").contentType("application/json").content("{\"endereco\":\"invalido\",\"usuarioId\":"+cliente+"}")).andExpect(status().isBadRequest());
  mvc.perform(get("/empresas").param("desconhecidoId","123")).andExpect(status().isBadRequest());
 }
}
