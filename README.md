# ATVII - AutoBots: maturidade REST e HATEOAS

API para cadastro de clientes, documentos, telefones e endereços, desenvolvida com Java 17, Spring Boot 4.1.1, Spring Data JPA e banco H2. Continuação da ATVI, baseada no projeto fornecido pelo professor Gerson Penha. Implementa os níveis do Richardson Maturity Model (RMM), com Spring HATEOAS e respostas HAL.

## Como executar

Requisitos: **JDK 17**, variável `JAVA_HOME` apontando para a pasta do JDK e internet na primeira execução. O Maven Wrapper está incluído; não é necessário instalar Maven ou um servidor de banco de dados.

1. Extraia o ZIP e entre na pasta **`AutoBots-ATVII/automanager`**, onde estão `pom.xml`, `mvnw` e `mvnw.cmd`.
2. No Windows, abra essa pasta no Explorador, digite `powershell` na barra de endereço e pressione Enter.
3. Confira o Java:

   ```powershell
   java -version
   javac -version
   ```

   Se `JAVA_HOME` ainda não estiver configurado, defina-o nessa janela. Substitua o caminho abaixo pela pasta do seu JDK:

   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   Test-Path "$env:JAVA_HOME\bin\javac.exe"
   ```

   O último comando deve retornar `True`. Essa configuração vale apenas para a janela atual.

4. Execute os testes e compile:

   ```powershell
   .\mvnw.cmd clean verify
   ```

   Aguarde **`BUILD SUCCESS`**. A entrega foi verificada com **44 testes sem falhas**. A primeira execução pode demorar para baixar as dependências.

5. Inicie a aplicação:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Aguarde a mensagem `Started AutomanagerApplication` e acesse **http://localhost:8080/** para descobrir os links dos quatro recursos. As listagens são objetos HAL com `_links` e, quando há registros, `_embedded`.

A porta padrão é **8080**. Mantenha o terminal aberto; para encerrar, pressione **Ctrl+C**. A aplicação é uma API: use as rotas abaixo para acessá-la; a raiz `/` retorna o índice de navegação em JSON, não uma página HTML.

Para usar outra porta, por exemplo 8081:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Nesse caso, use `http://localhost:8081` em todas as chamadas.

No **Linux/Ubuntu**, com JDK 17 e `JAVA_HOME` configurados, abra o terminal na pasta `automanager` e execute:

```sh
sh mvnw clean verify
sh mvnw spring-boot:run
```

Depois de compilar, também é possível iniciar pelo JAR, no Windows ou Linux:

```sh
java -jar target/automanager-0.0.1-SNAPSHOT.jar
```

Escolha uma forma de inicialização por vez. O H2 cria o banco na pasta **`automanager/data`** e mantém os dados entre execuções. Inicie sempre a partir da pasta `automanager`.

## Rotas

URL base: **`http://localhost:8080`**. Envie JSON com `Content-Type: application/json`.

| Método | Rota | Operação |
|---|---|---|
| GET | `/clientes` | Listar clientes |
| GET | `/clientes/{id}` | Consultar cliente |
| POST | `/clientes` | Cadastrar cliente e, opcionalmente, seus dados associados |
| PUT | `/clientes/{id}` | Substituir os dados editáveis do cliente |
| PATCH | `/clientes/{id}` | Atualizar parcialmente o cliente |
| DELETE | `/clientes/{id}` | Excluir cliente e seus dados associados |
| GET | `/documentos` | Listar documentos |
| GET | `/documentos/{id}` | Consultar documento |
| POST | `/documentos?clienteId={clienteId}` | Cadastrar documento no cliente |
| PUT | `/documentos/{id}` | Substituir os dados editáveis do documento |
| PATCH | `/documentos/{id}` | Atualizar parcialmente o documento |
| DELETE | `/documentos/{id}` | Excluir documento |
| GET | `/telefones` | Listar telefones |
| GET | `/telefones/{id}` | Consultar telefone |
| POST | `/telefones?clienteId={clienteId}` | Cadastrar telefone no cliente |
| PUT | `/telefones/{id}` | Substituir os dados editáveis do telefone |
| PATCH | `/telefones/{id}` | Atualizar parcialmente o telefone |
| DELETE | `/telefones/{id}` | Excluir telefone |
| GET | `/enderecos` | Listar endereços |
| GET | `/enderecos/{id}` | Consultar endereço |
| POST | `/enderecos?clienteId={clienteId}` | Cadastrar endereço no cliente |
| PUT | `/enderecos/{id}` | Substituir os dados editáveis do endereço |
| PATCH | `/enderecos/{id}` | Atualizar parcialmente o endereço |
| DELETE | `/enderecos/{id}` | Excluir endereço |

Use os IDs retornados pela API. Nos cadastros de documento, telefone e endereço, `clienteId` é opcional: sem ele, o registro é criado sem vínculo com cliente. As rotas originais `/cliente/cadastro`, `/cliente/clientes`, `/cliente/cliente/{id}`, `/cliente/atualizar` e `/cliente/excluir` também continuam disponíveis; nas duas últimas, envie `id` no corpo. O PUT legado `/cliente/atualizar` mantém a atualização parcial; para novas integrações, use PATCH nas rotas plurais.

## Exemplos de JSON

Use Postman, Insomnia ou outro cliente HTTP para enviar os exemplos abaixo.

### Cadastrar cliente — POST /clientes

É possível enviar apenas `nome` ou cadastrar os dados associados juntos. Os IDs são gerados pelo banco; não os informe no cadastro. `dataCadastro` é preenchida automaticamente quando omitida. Datas usam `yyyy-MM-dd`.

```json
{
  "nome": "Ana Silva",
  "nomeSocial": "Ana",
  "dataNascimento": "2000-05-15",
  "endereco": {
    "estado": "SP",
    "cidade": "Santos",
    "bairro": "Centro",
    "rua": "Rua das Flores",
    "numero": "100",
    "codigoPostal": "11000000",
    "informacoesAdicionais": "Apartamento 12"
  },
  "documentos": [{"tipo": "RG", "numero": "123456789"}],
  "telefones": [{"ddd": "13", "numero": "999999999"}]
}
```

### Atualizar parcialmente o cliente — PATCH /clientes/{id}

```json
{"nome": "Ana Souza"}
```

As atualizações por **PATCH** são **parciais**: campos omitidos ou nulos são preservados. Documentos e telefones enviados na atualização do cliente são adicionados quando não têm ID, ou atualizados quando o ID pertence ao cliente. Para removê-los, use suas rotas DELETE; listas vazias não removem registros.

### Documento — POST /documentos?clienteId={clienteId} ou PATCH /documentos/{id}

```json
{"tipo": "RG", "numero": "987654321"}
```

### Telefone — POST /telefones?clienteId={clienteId} ou PATCH /telefones/{id}

```json
{"ddd": "11", "numero": "988887777"}
```

### Endereço — POST /enderecos?clienteId={clienteId} ou PATCH /enderecos/{id}

```json
{
  "estado": "SP",
  "cidade": "São Paulo",
  "bairro": "Centro",
  "rua": "Rua A",
  "numero": "20",
  "codigoPostal": "01001000",
  "informacoesAdicionais": "Casa"
}
```

Cada cliente pode ter **um endereço**. Se já existir, use PATCH para atualizar parcialmente ou PUT com o endereço completo. Excluir um documento, telefone ou endereço desfaz o vínculo e preserva o cliente; excluir o cliente remove todos os seus dados associados.

São obrigatórios: nome do cliente; tipo e número do documento; DDD e número do telefone; cidade, rua e número do endereço. O número do documento deve ser único.

## Respostas

- **200**: consulta ou atualização realizada.
- **201**: cadastro criado, com registro no corpo e URL no cabeçalho `Location`.
- **204**: exclusão realizada, sem corpo na resposta.
- **400**: dados inválidos ou IDs incompatíveis.
- **404**: registro ou cliente não encontrado.
- **409**: conflito, como documento duplicado ou segundo endereço para o mesmo cliente.

## O que esta continuação entrega

O enunciado da ATVII parte dos CRUDs concluídos na ATVI e solicita a implementação dos níveis RMM. O projeto do professor inicia a adição de links para clientes. Esta entrega estende a ideia aos quatro recursos e mantém a estrutura Java/Spring da primeira atividade.

| Nível | Evidência no projeto |
|---|---|
| 0 - HTTP | API acessível por HTTP; requisições JSON e respostas JSON/HAL |
| 1 - Recursos | URIs de coleção e de item: `/clientes`, `/clientes/{id}`, e equivalentes para documentos, telefones e endereços |
| 2 - Semântica HTTP | GET consulta; POST cria com 201 e Location; PUT substitui; PATCH altera parcialmente; DELETE responde 204; erros 400, 404, 405, 406, 409 e 415 |
| 3 - HATEOAS | Entrada `/`, `_links` nas coleções e itens, links nos dados associados, navegação e ações disponíveis conforme o estado |

Os níveis são cumulativos: alcançar o nível 3 aproveita os anteriores. Não são quatro aplicações ou quatro versões separadas. RMM descreve a maturidade da interface HTTP; não exige dividir cada entidade em um processo diferente.

### Contrato HAL e links

As consultas aceitam `Accept: application/hal+json` ou `application/json`. O formato de resposta dos recursos é HAL (`application/hal+json`). Corpos enviados em POST, PUT e PATCH usam `Content-Type: application/json`.

Exemplo resumido de uma listagem com um cliente:

```json
{
  "_embedded": {
    "clientes": [{
      "id": 1,
      "nome": "Ana",
      "_links": {
        "self": {"href": "http://localhost:8080/clientes/1"},
        "collection": {"href": "http://localhost:8080/clientes"},
        "atualizar": {"href": "http://localhost:8080/clientes/1"},
        "excluir": {"href": "http://localhost:8080/clientes/1"}
      }
    }]
  },
  "_links": {
    "self": {"href": "http://localhost:8080/clientes"},
    "cadastrar": {"href": "http://localhost:8080/clientes"},
    "inicio": {"href": "http://localhost:8080/"}
  }
}
```

Coleções vazias continuam oferecendo `_links`; o consumidor deve aceitar a ausência de `_embedded`. O exemplo é abreviado: clientes também contêm datas, associações e os links de cadastro dos dados associados. Os IDs ilustrativos devem ser substituídos pelos retornados em cada execução.

| Relação (`rel`) | Como usar |
|---|---|
| `self` | GET da própria representação |
| `inicio` | GET da entrada da API |
| `collection` | GET da coleção do item |
| `clientes`, `documentos`, `telefones`, `enderecos` | GET das coleções descobertas na entrada |
| `cadastrar` | POST de um novo registro |
| `atualizar` | PATCH parcial ou PUT completo no item |
| `excluir` | DELETE do item |
| `cadastrar-documento`, `cadastrar-telefone` | POST com vínculo ao cliente; `clienteId` já está no link |
| `cadastrar-endereco` | POST disponível somente quando o cliente não possui endereço |
| `endereco` | GET do endereço atual do cliente |
| `cliente` | GET do cliente proprietário do documento, telefone ou endereço; ausente em registros avulsos |

HAL não inclui o método HTTP em cada link: a tabela acima define a semântica das relações desta API. Não foi usado HAL-FORMS. Links são construídos com `linkTo(methodOn(...))`, a partir dos métodos dos controllers e do contexto da requisição, sem escrever as rotas novamente no montador nem fixar `localhost` no código. Cada documento, telefone e endereço retornado dentro de um cliente também possui seu próprio `self`.

### Diferença entre PUT e PATCH

- **PATCH** preserva campos omitidos/nulos e faz a atualização parcial herdada da ATVI. Para remover um filho, use DELETE; uma coleção vazia no PATCH não apaga os registros.
- **PUT** exige os campos obrigatórios e substitui os campos editáveis. Opcionais omitidos são limpos. No cliente, listas omitidas ou vazias removem os filhos correspondentes; endereço omitido remove o endereço. `id` e `dataCadastro` do cliente são preservados. Não use PUT com apenas um campo se quiser preservar os demais.
- No PUT de cliente, IDs de filhos informados devem pertencer a ele. Sem ID, documentos são reconhecidos pelo número e telefones por DDD+número; registros novos são criados. Repetir o mesmo PUT não duplica esses filhos. O endereço existente mantém sua identidade. Filhos omitidos são excluídos por `orphanRemoval`.
- PUT de um ID inexistente responde 404; esta API não faz criação por PUT. IDs incompatíveis no corpo respondem 400.

Exemplo de substituição de cliente sem associações:

```http
PUT /clientes/1
Content-Type: application/json

{"nome":"Ana Souza","nomeSocial":"Ana","dataNascimento":"2000-05-15","documentos":[],"telefones":[],"endereco":null}
```

## Demonstração para apresentação

1. Execute a aplicação conforme as instruções iniciais.
2. Em outro terminal, na pasta que contém este README, execute:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\demonstrar.ps1
   ```

   Para outra porta, acrescente `-BaseUrl http://localhost:8081`.

3. O roteiro conhece somente a URL inicial. Descobre os demais endereços nos links, cadastra um cliente e seus três tipos de dados associados, consulta os links, navega de cada filho de volta ao cliente e usa PATCH. Ao terminar, remove apenas o cliente de demonstração e seus dados associados.
4. Mostre o JSON: explique `self`, `collection`, as ações e por que o link `cadastrar-endereco` desaparece após o cadastro do endereço.
5. Execute `mvnw.cmd clean verify` na pasta `automanager` para demonstrar os testes.

Para importar no Eclipse: **File > Import > Maven > Existing Maven Projects**, selecione `automanager` e use JDK 17. Execute `AutomanagerApplication` como aplicação Java/Spring Boot.

## Organização e verificação

- `controles`: rotas, métodos HTTP, códigos de resposta e representação HAL.
- `modelo/Hipermidia.java`: links dos itens, coleções e associações, incluindo as ações dependentes do estado.
- `modelo/ClienteSubstituidor.java`: substituição completa e idempotente do agregado cliente.
- `servicos` e `repositorios`: regras, transações e persistência; operações inválidas não deixam alterações parciais.
- `dto`: classes `*Entrada` e `*Saida` separadas; apenas as saídas estendem `RepresentationModel`, com nomes de coleção explícitos. `ConversorDto` mapeia os campos permitidos, incluindo dados associados.
- `entidades`: quatro entidades responsáveis pela persistência e pelas validações de domínio, sem dependência de HATEOAS.
- `controles/RotasLegadasControle.java`: preserva os caminhos antigos; os links das respostas apontam sempre para os controllers canônicos.
- `repositorios/VinculoCliente.java`: projeção dos IDs para os links de retorno; as listagens consultam os vínculos em lote, e os filhos aninhados usam o ID do cliente já conhecido.
- `HipermidiaApiTests`: navegação real pelos links com MockMvc, coleções vazias, Location, contexto/host, associações, erros de negociação, distinção PUT/PATCH, retorno ao cliente proprietário, registros avulsos, compatibilidade das rotas antigas e isolamento dos links enviados no corpo.
- `CrudApiTests` e `AtualizadoresTests`: regressão dos CRUDs, validações, duplicidades, vínculos, datas e exclusões.

Verificação realizada em 18/10/2026 com JDK 17: **44 testes, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS**. O JAR compilado também foi iniciado e o roteiro de demonstração executado contra o servidor HTTP. Os testes usam H2 em memória; a execução padrão usa H2 em arquivo, sem exigir instalação de banco externo.

As listagens da ATVI mudaram de array simples para HAL e o PUT canônico passou a substituir dados. Clientes antigos devem ler `_embedded` e usar PATCH quando desejarem atualização parcial.

## Referências

- Primeira atividade: https://github.com/felipesgb/AV1-WEBIII (base: `f9b0b7970daa7f5741becd96b819ea7c321c8b4e`).
- Projeto inicial indicado no enunciado: https://github.com/gerson-pn/atvii-autobots-microservico-spring.
- Spring HATEOAS: https://docs.spring.io/spring-hateoas/docs/current/reference/html/.
- Integração com Spring Boot: https://docs.spring.io/spring-boot/reference/web/spring-hateoas.html.
