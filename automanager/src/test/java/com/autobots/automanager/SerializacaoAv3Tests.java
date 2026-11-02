package com.autobots.automanager;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sem transacao no teste: a serializacao ocorre depois de fechar a sessao do controle. */
@SpringBootTest @AutoConfigureMockMvc
class SerializacaoAv3Tests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Test void consultaPerfisForaDaSessaoJpa() throws Exception {
  long empresa=json.readTree(mvc.perform(post("/empresas").contentType("application/json").content("{\"razaoSocial\":\"Serializacao\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("id").asLong();
  long usuario=0;
  try {
   usuario=json.readTree(mvc.perform(post("/usuarios").contentType("application/json").content("{\"nome\":\"Ana\",\"perfis\":[\"CLIENTE\"],\"empresaId\":"+empresa+"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("id").asLong();
   mvc.perform(get("/usuarios/"+usuario)).andExpect(status().isOk()).andExpect(jsonPath("$.perfis[0]").value("CLIENTE"));
   mvc.perform(get("/usuarios").param("empresaId",String.valueOf(empresa))).andExpect(status().isOk());
  } finally {
   if(usuario!=0) mvc.perform(delete("/usuarios/"+usuario)).andExpect(status().isNoContent());
   mvc.perform(delete("/empresas/"+empresa)).andExpect(status().isNoContent());
  }
 }
}
