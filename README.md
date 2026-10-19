# AV2-WEBIII - AutoBots

API para cadastro de clientes, documentos, telefones e endereços, desenvolvida com **Java 17, Spring Boot 4.1.1, Spring Data JPA, Spring HATEOAS e H2**. Esta atividade continua a ATVI: mantém os CRUDs e implementa os níveis do Richardson Maturity Model (RMM), incluindo navegação por links em respostas HAL.

Este guia mantém a organização da primeira atividade: execução, rotas, exemplos e respostas. Inclui também um roteiro de teste da ATVII e soluções para os problemas mais comuns de configuração.

## Como executar no Windows

### 1. Obter o projeto e abrir a pasta correta

Requisitos: **JDK 17** (inclui `java` e `javac`), PowerShell e conexão à internet para baixar as dependências na primeira execução. Não é necessário instalar Maven, MySQL, Docker ou Postman. O banco H2 e o Maven Wrapper acompanham a configuração do projeto.

Se o JDK 17 não estiver instalado, instale um pacote **JDK 17**, por exemplo o [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17), escolhendo o sistema e a arquitetura do computador. Reabra o PowerShell após a instalação. Somente o JRE não é suficiente para compilar.

**Com Git**, abra o PowerShell na pasta em que deseja salvar o projeto:

```powershell
git clone https://github.com/felipesgb/AV2-WEBIII.git
cd .\AV2-WEBIII\automanager
```

**Sem Git**, use **Code > Download ZIP** no GitHub, extraia tudo e abra a pasta `automanager` que está dentro da pasta extraída. Não execute dentro do ZIP. No Explorador de Arquivos, abra `automanager`, digite `powershell` na barra de endereço e pressione Enter.

A estrutura é esta; o nome da pasta externa pode variar no ZIP (por exemplo, `AV2-WEBIII-main`):

```text
AV2-WEBIII/
├── README.md
└── automanager/          <-- primeiro terminal aqui
    ├── pom.xml
    ├── mvnw.cmd
    ├── mvnw
    ├── .mvn/wrapper/
    └── src/
```

Confira a pasta atual:

```powershell
Get-Location
Test-Path .\pom.xml
Test-Path .\mvnw.cmd
Test-Path .\.mvn\wrapper\maven-wrapper.properties
```

Os três `Test-Path` devem retornar **True**. Todos os comandos Maven e Java abaixo são executados dentro de `automanager`.

### 2. Configurar o Java neste terminal

O caminho do JDK varia entre computadores: pode terminar em `jdk-17.0.20.1`, `jdk-17...-hotspot` ou outro nome. **Não é necessário renomear a pasta nem copiar o caminho do computador do autor.**

Localize a instalação do JDK no Explorador. Estas pastas são locais comuns:

```powershell
Get-ChildItem "$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Microsoft", "$env:ProgramFiles\Amazon Corretto" -Directory -ErrorAction SilentlyContinue | Select-Object FullName
```

Copie o caminho real da pasta do **JDK 17** que contém a subpasta `bin`. Execute o bloco abaixo e cole esse caminho quando solicitado, sem acrescentar `\bin`:

```powershell
$env:JAVA_HOME = (Read-Host 'Cole o caminho completo da pasta do JDK 17, sem aspas').Trim().Trim('"')
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) { throw 'java.exe nao encontrado. Confira o caminho do JDK.' }
if (-not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) { throw 'javac.exe nao encontrado. Use um JDK, nao apenas um JRE.' }
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "$env:JAVA_HOME\bin\java.exe" -version
& "$env:JAVA_HOME\bin\javac.exe" -version
.\mvnw.cmd -version
```

As versões devem indicar **17**. `JAVA_HOME` não deve terminar em `java.exe`, `bin` ou apontar para `Common Files\Oracle\Java\javapath`; esta última é uma pasta de atalhos, não a instalação do JDK. O `&` permite executar um caminho que contém espaços. Essa configuração vale apenas para esta janela do PowerShell e não altera as configurações globais do Windows.

**Se fechar este terminal, repita a configuração do Java na próxima janela antes de usar Maven.**

### 3. Testar e compilar

```powershell
.\mvnw.cmd clean verify
```

Na primeira execução, aguarde o download do Maven e das dependências. O resultado esperado é:

```text
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Os testes cobrem CRUDs, validações, conflitos, transações, links HATEOAS, vínculos com clientes, rotas antigas e diferenças entre PUT e PATCH. Usam **H2 em memória**, sem alterar o banco da execução normal.

Alguns testes provocam duplicidade e requisições inválidas de propósito: avisos de SQL/HTTP durante eles são esperados. A confirmação de sucesso é o resumo final acima. Não prossiga se houver `BUILD FAILURE`; consulte a seção de erros.

### 4. Iniciar a aplicação

No mesmo terminal, ainda em `automanager`:

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde **`Started AutomanagerApplication`**. Abra [http://localhost:8080/](http://localhost:8080/) no navegador. Deve aparecer JSON com `_links` para `clientes`, `documentos`, `telefones` e `enderecos`. Esta entrega é uma API; a página inicial é JSON, não uma interface gráfica.

Mantenha esse terminal aberto enquanto usa a API. Para parar, pressione **Ctrl+C**. Não inicie outra cópia na mesma porta ou usando o mesmo arquivo de banco.

Se a porta 8080 já estiver ocupada, use 8081:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Nesse caso, use `http://localhost:8081` no navegador e nas requisições.

### 5. Testar a API em um segundo terminal

Com o servidor rodando, abra **outro PowerShell**. Esse terminal não precisa configurar Java, pois só enviará requisições HTTP.

Confira a entrada da API:

```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/' | ConvertTo-Json -Depth 10
```

Os comandos abaixo cadastram, consultam, atualizam com PATCH e PUT e excluem um cliente. Se usou a porta 8081, altere `$base`:

```powershell
$base = 'http://localhost:8080'

# Cadastrar (201) e guardar o ID retornado
$c = Invoke-RestMethod -Method Post -Uri "$base/clientes" -ContentType 'application/json' -Body '{"nome":"Ana Silva"}'
$c.id

# Consultar (a resposta traz _links)
Invoke-RestMethod -Uri "$base/clientes/$($c.id)" | ConvertTo-Json -Depth 10

# Atualizar parcialmente (PATCH)
Invoke-RestMethod -Method Patch -Uri "$base/clientes/$($c.id)" -ContentType 'application/json' -Body '{"nomeSocial":"Ana"}'

# Substituir (PUT exige os campos obrigatórios)
Invoke-RestMethod -Method Put -Uri "$base/clientes/$($c.id)" -ContentType 'application/json' -Body '{"nome":"Ana Souza"}'

# Excluir (204, sem corpo)
Invoke-RestMethod -Method Delete -Uri "$base/clientes/$($c.id)"
```

Para testar documentos, telefones e endereços, ou cadastrar dados que permaneçam no banco, use Postman/Insomnia com os exemplos da seção **Exemplos de JSON**.

## Outras formas de executar

### JAR compilado

Depois de `clean verify`, o arquivo fica em `automanager/target/automanager-0.0.1-SNAPSHOT.jar`. Pare a execução anterior com Ctrl+C e, na pasta `automanager`, execute:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar .\target\automanager-0.0.1-SNAPSHOT.jar
```

Para outra porta, acrescente `--server.port=8081` ao final. Escolha **Maven ou JAR**, uma execução por vez.

### Banco e dados da avaliação

A execução padrão cria o banco H2 em **`automanager/data`** e mantém os dados ao reiniciar. Não exige instalação/configuração de um servidor de banco. Inicie sempre a partir de `automanager`, pois o caminho do banco é relativo à pasta atual. `clean verify` não apaga `data`.

Se preferir uma avaliação temporária, sem gravar dados em arquivo, pare o servidor e execute o JAR com banco em memória:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar .\target\automanager-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:h2:mem:avaliacao --spring.jpa.hibernate.ddl-auto=create-drop
```

Os dados dessa execução desaparecem ao encerrar a aplicação. O banco em arquivo anterior permanece intacto. Acrescente `--server.port=8081` se necessário.

### Eclipse

1. Em **File > Import > Maven > Existing Maven Projects**, selecione a pasta `automanager` (a que contém `pom.xml`).
2. Em **Window > Preferences > Java > Installed JREs**, configure um **JDK 17**. Confira também o JDK do projeto e da configuração de execução.
3. Aguarde o Maven resolver as dependências. Se necessário, use **Maven > Update Project**.
4. Execute `AutomanagerApplication` como **Java Application** ou **Spring Boot App**. Configure a pasta de trabalho da execução como `automanager` para manter o banco no mesmo local.
5. Acesse a raiz e teste conforme o passo 5. Pare o servidor iniciado no terminal antes de iniciar pela IDE.

### Linux/Ubuntu e macOS

Instale um **JDK 17** e abra o terminal na pasta `automanager`. Confira `java -version` e `javac -version`. `JAVA_HOME` deve apontar para a instalação do JDK 17.

No Linux, se o `javac` selecionado já é o 17, é possível descobrir o caminho assim:

```sh
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
export PATH="$JAVA_HOME/bin:$PATH"
```

No macOS, com um JDK 17 instalado:

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export PATH="$JAVA_HOME/bin:$PATH"
```

Em ambos:

```sh
sh mvnw -version
sh mvnw clean verify
sh mvnw spring-boot:run
```

Usar `sh mvnw` evita depender da permissão de execução do arquivo. O servidor usa a mesma porta e as mesmas rotas. Para testar a API, use `curl` ou Postman/Insomnia com os exemplos da seção **Exemplos de JSON**; os 44 testes de `clean verify` não dependem de PowerShell.

Os comandos Linux/macOS são alternativas de execução.

## Erros comuns e como resolver

| Mensagem ou situação | O que conferir/fazer |
|---|---|
| `JAVA_HOME not found` ou `JAVA_HOME is set to an invalid directory` | Repita o passo 2 na mesma janela que executará Maven. Confira a pasta real do JDK, sem `bin` no final. |
| `java` funciona, mas Maven usa outra versão | Execute `.\mvnw.cmd -version`. Maven usa `JAVA_HOME`; configure o JDK 17 no passo 2, mesmo que `java -version` funcione. |
| `javac.exe` não existe ou `No compiler is provided` | A instalação é apenas JRE ou o caminho está errado. Use um **JDK 17** com `bin\javac.exe`. |
| `mvnw.cmd` não é reconhecido | Use `.\mvnw.cmd`, com ponto e barra, dentro de `automanager`. |
| `no POM`, `.mvn` ausente ou `MavenWrapperMain` não encontrado | Extraia/baixe o projeto inteiro, incluindo `.mvn/wrapper`, e confira os três `Test-Path` do passo 1. Não copie somente `src` e não invoque o wrapper de outra pasta. |
| `release version 17 not supported`, versão de classe incompatível ou erro de Lombok/compilador | Confira `.\mvnw.cmd -version` e o JDK da IDE; use o **JDK 17** validado para esta entrega e execute `clean verify` novamente. |
| Download de dependências falha (`Could not transfer artifact`, timeout, `PKIX`) | Confira acesso à internet e a `https://repo.maven.apache.org`, data/hora do computador e proxy/certificados da instituição. Após resolver a rede, execute `.\mvnw.cmd -U clean verify`. Não desative a validação TLS. |
| `Port 8080 was already in use` | Pare a outra execução com Ctrl+C ou use 8081 conforme o passo 4; ajuste também a URL das requisições. |
| `Database may be already in use` / banco bloqueado | Pare as outras instâncias que usam esse projeto. Mudar apenas a porta não libera o banco em arquivo; para uma execução independente, use a opção de banco em memória. |
| Banco antigo com esquema incompatível | Para avaliar sem alterar os dados antigos, pare o servidor e use o JAR com H2 em memória, conforme a seção de banco. |
| `Unable to access jarfile` | Execute `clean verify`, confira o `BUILD SUCCESS` e rode o comando do JAR dentro de `automanager`. |
| Conexão recusada no navegador ou cliente HTTP | Aguarde `Started AutomanagerApplication`, mantenha o primeiro terminal aberto e confira se a porta do servidor e a URL são iguais. |
| Resposta vazia, só `_links` ou ausência de `_embedded` | Pode ser uma coleção sem registros. Cadastre um registro (passo 5) e consulte de novo. |
| `400 Bad Request` | Confira campos obrigatórios, tipos, datas `yyyy-MM-dd` e IDs. PUT exige os dados completos; para alterar só um campo, use PATCH. |
| `404 Not Found` | Use os IDs/links retornados pela API; não presuma que o primeiro ID é sempre 1. Um registro excluído retorna 404. |
| `409 Conflict` | Use outro número de documento. Se o cliente já tem endereço, atualize o existente em vez de cadastrar outro. |
| `405`, `406` ou `415` | Confira respectivamente o método HTTP, `Accept: application/hal+json` (ou JSON) e `Content-Type: application/json`. |

Se `clean verify` falhar, os detalhes ficam em `automanager/target/surefire-reports`. Diferencie **avisos dos cenários negativos** de uma falha real no resumo final. Não use `-DskipTests` para validar a entrega.


## Organização do código

```text
com.autobots.automanager
├── controles      # endpoints REST, entrada da API (InicioControle), rotas originais (RotasLegadasControle) e TratadorErros
├── servicos       # regras de negócio e validações
├── repositorios   # acesso a dados (Spring Data JPA)
├── entidades      # Cliente, Documento, Endereco e Telefone
├── dto            # objetos de entrada e de saída (HATEOAS) e conversor
└── modelo         # atualizadores, substituidor de cliente e Hipermidia (montagem dos links)
```

Os testes ficam em `automanager/src/test`: `CrudApiTests` (CRUDs, validações e conflitos), `HipermidiaApiTests` (links HATEOAS), `AtualizadoresTests` e `AutomanagerApplicationTests`.

## Rotas

URL base: **`http://localhost:8080`**. Envie JSON com `Content-Type: application/json`.

| Método | Rota | Operação | Status |
|---|---|---|---|
| GET | `/clientes` | Listar clientes | `200` |
| GET | `/clientes/{id}` | Consultar cliente | `200` |
| POST | `/clientes` | Cadastrar cliente e, opcionalmente, seus dados associados | `201` |
| PUT | `/clientes/{id}` | Substituir os dados editáveis do cliente | `200` |
| PATCH | `/clientes/{id}` | Atualizar parcialmente o cliente | `200` |
| DELETE | `/clientes/{id}` | Excluir cliente e seus dados associados | `204` |
| GET | `/documentos` | Listar documentos | `200` |
| GET | `/documentos/{id}` | Consultar documento | `200` |
| POST | `/documentos?clienteId={clienteId}` | Cadastrar documento no cliente | `201` |
| PUT | `/documentos/{id}` | Substituir os dados editáveis do documento | `200` |
| PATCH | `/documentos/{id}` | Atualizar parcialmente o documento | `200` |
| DELETE | `/documentos/{id}` | Excluir documento | `204` |
| GET | `/telefones` | Listar telefones | `200` |
| GET | `/telefones/{id}` | Consultar telefone | `200` |
| POST | `/telefones?clienteId={clienteId}` | Cadastrar telefone no cliente | `201` |
| PUT | `/telefones/{id}` | Substituir os dados editáveis do telefone | `200` |
| PATCH | `/telefones/{id}` | Atualizar parcialmente o telefone | `200` |
| DELETE | `/telefones/{id}` | Excluir telefone | `204` |
| GET | `/enderecos` | Listar endereços | `200` |
| GET | `/enderecos/{id}` | Consultar endereço | `200` |
| POST | `/enderecos?clienteId={clienteId}` | Cadastrar endereço no cliente | `201` |
| PUT | `/enderecos/{id}` | Substituir os dados editáveis do endereço | `200` |
| PATCH | `/enderecos/{id}` | Atualizar parcialmente o endereço | `200` |
| DELETE | `/enderecos/{id}` | Excluir endereço | `204` |

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

## Formas de cadastro

**Tudo de uma vez** — envie cliente, documentos, endereço e telefones juntos em `POST /clientes`.

**Passo a passo** (útil para testes):

1. `POST /clientes` apenas com os dados básicos (`nome`).
2. `POST /documentos?clienteId={clienteId}` para cada documento.
3. `POST /enderecos?clienteId={clienteId}` para o endereço (`409` se o cliente já tiver um).
4. `POST /telefones?clienteId={clienteId}` para cada telefone.

## Respostas

- **200**: consulta ou atualização realizada.
- **201**: cadastro criado, com registro no corpo e URL no cabeçalho `Location`.
- **204**: exclusão realizada, sem corpo na resposta.
- **400**: dados inválidos ou IDs incompatíveis.
- **404**: registro ou cliente não encontrado.
- **409**: conflito, como documento duplicado ou segundo endereço para o mesmo cliente.

## Validações e erros

Campos obrigatórios são checados automaticamente (por exemplo, `nome` do cliente e `tipo`/`numero` do documento). Os erros voltam em JSON com `status` e `mensagem`:

```json
{"status": 404, "mensagem": "Cliente nao encontrado"}
```

| Situação | Status HTTP |
|---|---|
| Campo obrigatório ausente, JSON/data/parâmetro inválido, ID informado no cadastro, ID do corpo diferente da URL ou item que não pertence ao cliente | `400` |
| Registro ou cliente não encontrado | `404` |
| Documento duplicado ou cliente que já possui endereço | `409` |
| Método HTTP não suportado na rota | `405` |
| `Accept` não suportado | `406` |
| `Content-Type` diferente de `application/json` | `415` |

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
