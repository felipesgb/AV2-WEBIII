package com.autobots.automanager;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
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

@SpringBootTest
@AutoConfigureMockMvc
class HipermidiaApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ClienteRepositorio clientes;
    @Autowired DocumentoRepositorio documentos;
    @Autowired TelefoneRepositorio telefones;
    @Autowired EnderecoRepositorio enderecos;

    @BeforeEach void limpar() {
        clientes.deleteAll(); documentos.deleteAll(); telefones.deleteAll(); enderecos.deleteAll();
    }
    JsonNode ler(String url) throws Exception {
        return json.readTree(mvc.perform(get(URI.create(url)).accept("application/hal+json"))
            .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/hal+json"))
            .andReturn().getResponse().getContentAsString());
    }
    String href(JsonNode item, String rel) { return item.path("_links").path(rel).path("href").asText(); }
    JsonNode criar(String url, String corpo) throws Exception {
        var resposta = mvc.perform(post(URI.create(url)).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated()).andReturn().getResponse();
        JsonNode item = json.readTree(resposta.getContentAsString());
        assertThat(resposta.getHeader("Location")).isEqualTo(href(item, "self"));
        return item;
    }

    @ParameterizedTest
    @CsvSource({"clientes,nome", "documentos,tipo", "telefones,ddd", "enderecos,cidade"})
    void navegarDesdeInicioAteExcluir(String recurso, String campo) throws Exception {
        JsonNode inicio = ler("/");
        JsonNode lista = ler(href(inicio, recurso));
        assertThat(href(lista, "self")).endsWith("/" + recurso);
        assertThat(href(lista, "cadastrar")).isNotBlank();
        String corpo = switch (recurso) {
            case "clientes" -> "{\"nome\":\"Ana\"}";
            case "documentos" -> "{\"tipo\":\"RG\",\"numero\":\"123\"}";
            case "telefones" -> "{\"ddd\":\"11\",\"numero\":\"99999999\"}";
            default -> "{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"1\"}";
        };
        JsonNode item = criar(href(lista, "cadastrar"), corpo);
        assertThat(ler(href(item, "self")).path("id")).isEqualTo(item.path("id"));
        lista = ler(href(item, "collection"));
        JsonNode embutido = lista.path("_embedded").path(recurso).get(0);
        assertThat(href(embutido, "self")).isEqualTo(href(item, "self"));
        mvc.perform(patch(URI.create(href(item, "atualizar"))).contentType(MediaType.APPLICATION_JSON)
            .content("{\"" + campo + "\":\"22\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$." + campo).value("22"))
            .andExpect(jsonPath("$._links.self.href").value(href(item, "self")));
        assertThat(ler(href(item, "inicio")).path("_links").has(recurso)).isTrue();
        mvc.perform(delete(URI.create(href(item, "excluir"))))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(URI.create(href(item, "self")))).andExpect(status().isNotFound());
        assertThat(ler(href(item, "collection")).path("_embedded").path(recurso).size()).isZero();
    }

    @Test void vinculosDoClienteAcompanhamEstadoAtual() throws Exception {
        JsonNode c = criar("/clientes", "{\"nome\":\"Ana\"}");
        JsonNode endereco = criar(href(c, "cadastrar-endereco"), "{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"1\"}");
        JsonNode documento = criar(href(c, "cadastrar-documento"), "{\"tipo\":\"RG\",\"numero\":\"123\"}");
        JsonNode telefone = criar(href(c, "cadastrar-telefone"), "{\"ddd\":\"11\",\"numero\":\"99999999\"}");
        c = ler(href(c, "self"));
        assertThat(c.path("_links").has("cadastrar-endereco")).isFalse();
        assertThat(href(c, "endereco")).isEqualTo(href(endereco, "self"));
        assertThat(href(c.path("endereco"), "self")).isEqualTo(href(endereco, "self"));
        assertThat(href(c.path("documentos").get(0), "self")).isEqualTo(href(documento, "self"));
        assertThat(href(c.path("telefones").get(0), "self")).isEqualTo(href(telefone, "self"));
        ler(href(c, "endereco"));
        mvc.perform(delete(URI.create(href(endereco, "excluir")))).andExpect(status().isNoContent());
        assertThat(ler(href(c, "self")).path("_links").has("cadastrar-endereco")).isTrue();
    }

    @Test void respeitaContextoEHostSemDuplicarLinks() throws Exception {
        JsonNode c = criar("/clientes", "{\"nome\":\"Ana\"}");
        String path = "/api/clientes/" + c.path("id").asLong();
        for (int i = 0; i < 2; i++) {
            mvc.perform(get(path).contextPath("/api").with(request -> {
                request.setScheme("https"); request.setServerName("exemplo.test"); request.setServerPort(443); return request;
            })).andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("https://exemplo.test" + path))
                .andExpect(jsonPath("$._links.collection.href").value("https://exemplo.test/api/clientes"));
        }
    }

    @Test void metodoETipoDeConteudoIncorretos() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.TEXT_PLAIN).content("Ana"))
            .andExpect(status().isUnsupportedMediaType());
        mvc.perform(delete("/clientes")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/clientes").accept(MediaType.APPLICATION_XML)).andExpect(status().isNotAcceptable());
    }

    @ParameterizedTest
    @CsvSource({"clientes", "documentos", "telefones", "enderecos"})
    void putSubstituiEPatchPreserva(String recurso) throws Exception {
        String corpo = switch (recurso) {
            case "clientes" -> "{\"nome\":\"Ana\",\"nomeSocial\":\"Aninha\"}";
            case "documentos" -> "{\"tipo\":\"RG\",\"numero\":\"123\"}";
            case "telefones" -> "{\"ddd\":\"11\",\"numero\":\"99999999\"}";
            default -> "{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"1\",\"bairro\":\"Centro\"}";
        };
        JsonNode c = criar("/" + recurso, corpo);
        URI url = URI.create(href(c, "self"));
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk());
        String substituto = corpo.replace(",\"nomeSocial\":\"Aninha\"", "").replace(",\"bairro\":\"Centro\"", "");
        for (int i = 0; i < 2; i++) {
            mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(substituto))
                .andExpect(status().isOk()).andExpect(jsonPath("$._links.self.href").value(url.toString()));
        }
        JsonNode salvo = ler(url.toString());
        assertThat(salvo.path("id")).isEqualTo(c.path("id"));
        if (recurso.equals("clientes")) {
            assertThat(salvo.path("nomeSocial").isNull()).isTrue();
            assertThat(salvo.path("dataCadastro")).isEqualTo(c.path("dataCadastro"));
        }
        if (recurso.equals("enderecos")) assertThat(salvo.path("bairro").isNull()).isTrue();
    }

    @Test void putClienteIdempotenteERemoveFilhosOmitidos() throws Exception {
        String corpo = "{\"nome\":\"Ana\",\"documentos\":[{\"tipo\":\"RG\",\"numero\":\"123\"}],"
            + "\"telefones\":[{\"ddd\":\"11\",\"numero\":\"99999999\"}],"
            + "\"endereco\":{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"1\"}}";
        JsonNode c = criar("/clientes", corpo);
        URI url = URI.create(href(c, "self"));
        for (int i = 0; i < 2; i++) {
            mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isOk());
            JsonNode atual = ler(url.toString());
            assertThat(atual.path("documentos").get(0).path("id")).isEqualTo(c.path("documentos").get(0).path("id"));
            assertThat(atual.path("telefones").get(0).path("id")).isEqualTo(c.path("telefones").get(0).path("id"));
            assertThat(atual.path("endereco").path("id")).isEqualTo(c.path("endereco").path("id"));
        }
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Bia\"}"))
            .andExpect(status().isOk());
        assertThat(documentos.count()).isZero();
        assertThat(telefones.count()).isZero();
        assertThat(enderecos.count()).isZero();
        assertThat(clientes.count()).isEqualTo(1);
    }

    @Test void putNaoPodeApropriarFilhoDeOutroCliente() throws Exception {
        JsonNode a = criar("/clientes", "{\"nome\":\"Ana\",\"documentos\":[{\"tipo\":\"RG\",\"numero\":\"123\"}]}");
        JsonNode b = criar("/clientes", "{\"nome\":\"Bia\"}");
        String corpo = "{\"nome\":\"Invalido\",\"documentos\":[{\"id\":"
            + a.path("documentos").get(0).path("id").asLong() + ",\"tipo\":\"RG\",\"numero\":\"123\"}]}";
        mvc.perform(put(URI.create(href(b, "self"))).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest());
        assertThat(ler(href(b, "self")).path("nome").asText()).isEqualTo("Bia");
        assertThat(documentos.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"documentos,documentos", "telefones,telefones", "enderecos,endereco"})
    void navegarDosFilhosAoDonoSemAssociarRegistrosAvulsos(String recurso, String campo) throws Exception {
        JsonNode a = criar("/clientes", "{\"nome\":\"Ana\"}");
        JsonNode b = criar("/clientes", "{\"nome\":\"Bia\"}");
        String corpo = switch (recurso) {
            case "documentos" -> "{\"tipo\":\"RG\",\"numero\":\"%s\"}";
            case "telefones" -> "{\"ddd\":\"11\",\"numero\":\"%s\"}";
            default -> "{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"%s\"}";
        };
        String rel = "cadastrar-" + (recurso.equals("documentos") ? "documento" : recurso.equals("telefones") ? "telefone" : "endereco");
        JsonNode filhoA = criar(href(a, rel), corpo.formatted("101"));
        JsonNode filhoB = criar(href(b, rel), corpo.formatted("202"));
        JsonNode avulso = criar("/" + recurso, corpo.formatted("303"));
        for (JsonNode filho : new JsonNode[]{filhoA, filhoB}) {
            String esperado = filho == filhoA ? href(a, "self") : href(b, "self");
            assertThat(href(filho, "cliente")).isEqualTo(esperado);
            assertThat(href(ler(href(filho, "self")), "cliente")).isEqualTo(esperado);
            JsonNode dono = ler(href(filho, "cliente"));
            JsonNode aninhado = campo.equals("endereco") ? dono.path(campo) : dono.path(campo).get(0);
            assertThat(href(aninhado, "cliente")).isEqualTo(esperado);
            mvc.perform(patch(URI.create(href(filho, "atualizar"))).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$._links.cliente.href").value(esperado));
        }
        assertThat(avulso.path("_links").has("cliente")).isFalse();
        assertThat(ler(href(avulso, "self")).path("_links").has("cliente")).isFalse();
        JsonNode lista = ler("/" + recurso).path("_embedded").path(recurso);
        assertThat(lista.size()).isEqualTo(3);
        for (JsonNode item : lista) {
            if (item.path("id").equals(avulso.path("id"))) {
                assertThat(item.path("_links").has("cliente")).isFalse();
            } else {
                String esperado = item.path("id").equals(filhoA.path("id")) ? href(a, "self") : href(b, "self");
                assertThat(href(item, "cliente")).isEqualTo(esperado);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"cliente,clientes", "documento,documentos", "telefone,telefones", "endereco,enderecos"})
    void rotasLegadasGeramLinksCanonicos(String singular, String plural) throws Exception {
        String corpo = switch (singular) {
            case "cliente" -> "{\"nome\":\"Ana\"}";
            case "documento" -> "{\"tipo\":\"RG\",\"numero\":\"123\"}";
            case "telefone" -> "{\"ddd\":\"11\",\"numero\":\"99999999\"}";
            default -> "{\"cidade\":\"Santos\",\"rua\":\"A\",\"numero\":\"1\"}";
        };
        JsonNode item = criar("/" + singular + "/cadastro", corpo);
        long id = item.path("id").asLong();
        String canonico = "http://localhost/" + plural + "/" + id;
        assertThat(href(item, "self")).isEqualTo(canonico);
        for (String url : new String[]{"/" + singular + "/" + id, "/" + singular + "/" + singular + "/" + id,
                "/" + plural + "/" + singular + "/" + id}) {
            assertThat(href(ler(url), "self")).isEqualTo(canonico);
        }
        for (String url : new String[]{"/" + singular, "/" + singular + "/" + plural, "/" + plural + "/" + plural}) {
            assertThat(href(ler(url), "self")).isEqualTo("http://localhost/" + plural);
        }
        mvc.perform(put("/" + singular + "/atualizar").contentType(MediaType.APPLICATION_JSON).content("{\"id\":" + id + "}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$._links.self.href").value(canonico));
        mvc.perform(delete("/" + singular + "/excluir").contentType(MediaType.APPLICATION_JSON).content("{\"id\":" + id + "}"))
            .andExpect(status().isNoContent());
    }

    @Test void linksEnviadosNoCorpoNaoAlteramLinksGerados() throws Exception {
        JsonNode c = criar("/clientes", "{\"nome\":\"Ana\",\"_links\":{\"self\":{\"href\":\"https://exemplo.invalid/forjado\"}},"
            + "\"documentos\":[{\"tipo\":\"RG\",\"numero\":\"123\",\"clienteId\":999,"
            + "\"_links\":{\"cliente\":{\"href\":\"https://exemplo.invalid/outro\"}}}]}");
        assertThat(href(c, "self")).startsWith("http://localhost/clientes/");
        JsonNode d = c.path("documentos").get(0);
        assertThat(href(d, "cliente")).isEqualTo(href(c, "self"));
        assertThat(d.has("clienteId")).isFalse();
        assertThat(href(ler(href(d, "self")), "cliente")).isEqualTo(href(c, "self"));
    }
}
