package com.autobots.automanager;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.autobots.automanager.repositorios.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sem @Transactional: cada chamada atravessa e confirma a transacao real do servico.
@SpringBootTest
@AutoConfigureMockMvc
class CrudApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ClienteRepositorio clientes;
    @Autowired DocumentoRepositorio documentos;
    @Autowired EnderecoRepositorio enderecos;
    @Autowired TelefoneRepositorio telefones;

    @BeforeEach void limpar() {
        clientes.deleteAll(); documentos.deleteAll(); telefones.deleteAll(); enderecos.deleteAll();
    }
    JsonNode criar(String rota, String corpo) throws Exception {
        return json.readTree(mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString());
    }
    JsonNode buscar(String rota) throws Exception {
        return json.readTree(mvc.perform(get(rota)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    String corpo(String recurso) {
        switch (recurso) {
            case "clientes": return "{\"nome\":\"Ana\",\"nomeSocial\":\"Aninha\",\"dataNascimento\":\"2000-05-15\"}";
            case "documentos": return "{\"tipo\":\"RG\",\"numero\":\"123456\"}";
            case "enderecos": return "{\"cidade\":\"Sao Paulo\",\"rua\":\"Rua A\",\"numero\":\"10\",\"codigoPostal\":\"01001000\"}";
            default: return "{\"ddd\":\"11\",\"numero\":\"999999999\"}";
        }
    }
    @ParameterizedTest
    @CsvSource({"clientes,nome,Bia", "documentos,tipo,CPF", "enderecos,codigoPostal,12345000", "telefones,ddd,21"})
    void cicloCrudCompleto(String recurso, String campo, String valor) throws Exception {
        JsonNode criado = criar("/" + recurso, corpo(recurso));
        String rota = "/" + recurso + "/" + criado.get("id").asLong();
        assertThat(buscar(rota).get(campo).asText()).isEqualTo(criado.get(campo).asText());
        assertThat(buscar("/" + recurso).path("_embedded").path(recurso).size()).isEqualTo(1);
        mvc.perform(patch(rota).contentType(MediaType.APPLICATION_JSON).content("{\""+campo+"\":\""+valor+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$."+campo).value(valor));
        assertThat(buscar(rota).get(campo).asText()).isEqualTo(valor);
        mvc.perform(delete(rota)).andExpect(status().isNoContent());
        mvc.perform(get(rota)).andExpect(status().isNotFound());
        assertThat(buscar("/"+recurso).path("_embedded").path(recurso).size()).isZero();
    }
    @ParameterizedTest
    @CsvSource({"clientes", "documentos", "enderecos", "telefones"})
    void inexistentesEEntradasInvalidas(String recurso) throws Exception {
        String rota = "/"+recurso;
        mvc.perform(get(rota+"/999999")).andExpect(status().isNotFound());
        mvc.perform(patch(rota+"/999999").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete(rota+"/999999")).andExpect(status().isNotFound());
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content("{\"id\":5}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get(rota+"/abc")).andExpect(status().isBadRequest());
    }
    @Test void clienteComAgregadoEExclusaoEmCascata() throws Exception {
        JsonNode c = criar("/clientes", "{\"nome\":\"Ana\",\"endereco\":"+corpo("enderecos")+",\"documentos\":["+corpo("documentos")+"],\"telefones\":["+corpo("telefones")+"]}");
        assertThat(c.get("dataCadastro").isNull()).isFalse();
        assertThat(buscar("/clientes/"+c.get("id")).get("documentos").size()).isEqualTo(1);
        mvc.perform(delete("/clientes/"+c.get("id"))).andExpect(status().isNoContent());
        assertThat(documentos.count()).isZero(); assertThat(enderecos.count()).isZero(); assertThat(telefones.count()).isZero();
    }
    @ParameterizedTest
    @CsvSource({"documentos,documentos", "telefones,telefones", "enderecos,endereco"})
    void criarAtualizarExcluirVinculado(String recurso, String campo) throws Exception {
        long cliente = criar("/clientes", corpo("clientes")).get("id").asLong();
        long item = criar("/"+recurso+"?clienteId="+cliente, corpo(recurso)).get("id").asLong();
        JsonNode c = buscar("/clientes/"+cliente);
        JsonNode vinculado = campo.equals("endereco") ? c.get(campo) : c.get(campo).get(0);
        assertThat(vinculado.get("id").asLong()).isEqualTo(item);
        mvc.perform(patch("/"+recurso+"/"+item).contentType(MediaType.APPLICATION_JSON).content("{\"numero\":\"22222\"}"))
            .andExpect(status().isOk());
        c = buscar("/clientes/"+cliente);
        vinculado = campo.equals("endereco") ? c.get(campo) : c.get(campo).get(0);
        assertThat(vinculado.get("numero").asText()).isEqualTo("22222");
        mvc.perform(delete("/"+recurso+"/"+item)).andExpect(status().isNoContent());
        mvc.perform(get("/"+recurso+"/"+item)).andExpect(status().isNotFound());
        c = buscar("/clientes/"+cliente);
        if (campo.equals("endereco")) assertThat(c.get(campo).isNull()).isTrue();
        else assertThat(c.get(campo).size()).isZero();
    }
    @Test void enderecoUnicoEClienteInexistente() throws Exception {
        long id = criar("/clientes", corpo("clientes")).get("id").asLong();
        criar("/enderecos?clienteId="+id, corpo("enderecos"));
        mvc.perform(post("/enderecos?clienteId="+id).contentType(MediaType.APPLICATION_JSON).content(corpo("enderecos")))
            .andExpect(status().isConflict());
        for (String recurso : new String[]{"enderecos", "documentos", "telefones"}) {
            mvc.perform(post("/"+recurso+"?clienteId=999999").contentType(MediaType.APPLICATION_JSON).content(corpo(recurso)))
                .andExpect(status().isNotFound());
        }
        assertThat(enderecos.count()).isEqualTo(1);
    }
    @Test void duplicidadeRetorna409ESemPersistenciaParcial() throws Exception {
        criar("/documentos", corpo("documentos"));
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Ana\",\"documentos\":["+corpo("documentos")+"]}"))
            .andExpect(status().isConflict());
        assertThat(clientes.count()).isZero(); assertThat(documentos.count()).isEqualTo(1);
        long id = criar("/documentos", "{\"tipo\":\"RG\",\"numero\":\"outro\"}").get("id").asLong();
        mvc.perform(patch("/documentos/"+id).contentType(MediaType.APPLICATION_JSON).content("{\"numero\":\"123456\"}"))
            .andExpect(status().isConflict());
        assertThat(buscar("/documentos/"+id).get("numero").asText()).isEqualTo("outro");
    }
    @Test void atualizacaoParcialNulosNovosFilhosECep() throws Exception {
        long id = criar("/clientes", corpo("clientes")).get("id").asLong();
        mvc.perform(patch("/clientes/"+id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nomeSocial\":\"\",\"documentos\":null,\"telefones\":null}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana")).andExpect(jsonPath("$.nomeSocial").value(""));
        mvc.perform(patch("/clientes/"+id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"endereco\":"+corpo("enderecos")+",\"documentos\":["+corpo("documentos")+"],\"telefones\":["+corpo("telefones")+"]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.documentos.length()").value(1));
        mvc.perform(patch("/clientes/"+id).contentType(MediaType.APPLICATION_JSON).content("{\"endereco\":{\"codigoPostal\":\"55555555\"}}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.endereco.codigoPostal").value("55555555"));
        assertThat(buscar("/clientes/"+id).get("telefones").size()).isEqualTo(1);
    }
    @Test void impedeTrocaDeIdEFilhosDeOutroCliente() throws Exception {
        long a = criar("/clientes", corpo("clientes")).get("id").asLong();
        long b = criar("/clientes", corpo("clientes")).get("id").asLong();
        long d = criar("/documentos?clienteId="+a, corpo("documentos")).get("id").asLong();
        mvc.perform(patch("/clientes/"+b).contentType(MediaType.APPLICATION_JSON).content("{\"id\":"+a+"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/clientes/"+b).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Nao salvar\",\"documentos\":[{\"id\":"+d+",\"tipo\":\"CPF\"}]}"))
            .andExpect(status().isBadRequest());
        assertThat(buscar("/clientes/"+b).get("nome").asText()).isEqualTo("Ana");
        assertThat(buscar("/documentos/"+d).get("tipo").asText()).isEqualTo("RG");
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"C\",\"documentos\":[{\"id\":"+d+",\"tipo\":\"RG\",\"numero\":\"x\"}]}"))
            .andExpect(status().isBadRequest());
    }
    @Test void validaAtualizacaoEJson() throws Exception {
        long id = criar("/clientes", corpo("clientes")).get("id").asLong();
        mvc.perform(patch("/clientes/"+id).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
            .andExpect(status().isBadRequest());
        assertThat(buscar("/clientes/"+id).get("nome").asText()).isEqualTo("Ana");
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{invalido"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Ana\",\"documentos\":[null]}"))
            .andExpect(status().isBadRequest());
    }
    @Test void preservaFormatoDasDatasComJackson3() throws Exception {
        JsonNode cliente = criar("/clientes", corpo("clientes"));
        assertThat(cliente.get("dataNascimento").asText()).isEqualTo("2000-05-15");
        assertThat(cliente.get("dataCadastro").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
        JsonNode salvo = buscar("/clientes/" + cliente.get("id").asLong());
        assertThat(salvo.get("dataNascimento").asText()).isEqualTo("2000-05-15");
        assertThat(salvo.get("dataCadastro").asText()).isEqualTo(cliente.get("dataCadastro").asText());
    }
    @Test void compatibilidadeRotasOriginais() throws Exception {
        long id = criar("/cliente/cadastro", corpo("clientes")).get("id").asLong();
        mvc.perform(get("/cliente/cliente/"+id)).andExpect(status().isOk());
        mvc.perform(get("/cliente/clientes")).andExpect(status().isOk()).andExpect(jsonPath("$._embedded.clientes.length()").value(1));
        mvc.perform(put("/cliente/atualizar").contentType(MediaType.APPLICATION_JSON).content("{\"id\":"+id+",\"nome\":\"Bia\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Bia"));
        mvc.perform(delete("/cliente/excluir").contentType(MediaType.APPLICATION_JSON).content("{\"id\":"+id+"}"))
            .andExpect(status().isNoContent());
        mvc.perform(put("/cliente/atualizar").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }
}
