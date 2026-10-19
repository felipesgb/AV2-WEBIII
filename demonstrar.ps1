param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'

function Enviar($Url, $Metodo, $Dados) {
    $json = $Dados | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Uri $Url -Method $Metodo -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}

# O unico endereco conhecido inicialmente e a raiz. Os demais vem das respostas.
$inicio = Invoke-RestMethod -Uri "$($BaseUrl.TrimEnd('/'))/"
$colecao = Invoke-RestMethod -Uri $inicio._links.clientes.href
$cliente = Enviar $colecao._links.cadastrar.href 'Post' @{nome='Demonstracao ATVII'; nomeSocial='Demo'}
$urlCliente = $cliente._links.self.href
try {
    Write-Output 'Cliente criado; navegando pelos links retornados pela API.'
    $documento = Enviar $cliente._links.'cadastrar-documento'.href 'Post' @{tipo='DEMO'; numero=[guid]::NewGuid().ToString()}
    $telefone = Enviar $cliente._links.'cadastrar-telefone'.href 'Post' @{ddd='11'; numero='999999999'}
    $endereco = Enviar $cliente._links.'cadastrar-endereco'.href 'Post' @{cidade='Santos'; rua='Rua A'; numero='10'}
    foreach ($item in @($documento, $telefone, $endereco)) {
        $consulta = Invoke-RestMethod -Uri $item._links.self.href
        if ($consulta.id -ne $item.id) { throw 'O link retornou outro registro.' }
        if ($consulta._links.cliente.href -ne $urlCliente) { throw 'Link para o cliente incorreto.' }
        $dono = Invoke-RestMethod -Uri $consulta._links.cliente.href
        if ($dono.id -ne $cliente.id) { throw 'O link retornou outro cliente.' }
    }
    $cliente = Enviar $cliente._links.atualizar.href 'Patch' @{nome='Demonstracao atualizada'}
    if ($cliente.nomeSocial -ne 'Demo') { throw 'PATCH nao preservou o campo omitido.' }
    $cliente = Invoke-RestMethod -Uri $cliente._links.self.href
    if (-not $cliente._links.endereco.href) { throw 'Link do endereco ausente.' }
    if ($cliente._links.'cadastrar-endereco') { throw 'Acao de cadastrar segundo endereco nao deveria existir.' }
    $cliente | ConvertTo-Json -Depth 15
    Write-Output 'Navegacao nos dois sentidos, cadastro dos quatro recursos e atualizacao parcial: OK.'
} finally {
    Invoke-RestMethod -Uri $urlCliente -Method Delete | Out-Null
    Write-Output 'Dados desta demonstracao removidos em cascata.'
}
