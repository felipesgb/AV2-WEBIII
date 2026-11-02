param([string]$Base = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
$sufixo = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()
$placa = (-join (1..3 | ForEach-Object { [char](Get-Random -Minimum 65 -Maximum 91) })) + (Get-Random -Minimum 1000 -Maximum 10000)
function Criar($Recurso, $Dados) {
    $corpo = [Text.Encoding]::UTF8.GetBytes(($Dados | ConvertTo-Json -Depth 10))
    Invoke-RestMethod -Method Post -Uri "$Base/$Recurso" -ContentType 'application/json; charset=utf-8' -Body $corpo
}
$empresaId = Criar 'empresas' @{ razaoSocial = 'AutoBots Demonstração'; nomeFantasia = 'Oficina AV3' }
$clienteId = Criar 'usuarios' @{ nome = 'Ana'; perfis = @('CLIENTE'); empresaId = $empresaId.id }
$funcionarioId = Criar 'usuarios' @{ nome = 'Bia'; perfis = @('FUNCIONARIO'); empresaId = $empresaId.id }
$fornecedorId = Criar 'usuarios' @{ nome = 'Peças Ltda'; perfis = @('FORNECEDOR'); empresaId = $empresaId.id }
$veiculoId = Criar 'veiculos' @{ modelo = 'Gol'; placa = $placa; tipo = 'HATCH'; proprietarioId = $clienteId.id }
$mercadoriaId = Criar 'mercadorias' @{ nome = 'Filtro'; valor = 30.5; quantidade = 10; fabricacao = '2026-01-01'; validade = '2030-01-01'; empresaId = $empresaId.id; fornecedorId = $fornecedorId.id }
$servicoId = Criar 'servicos' @{ nome = 'Troca de filtro'; valor = 50; empresaId = $empresaId.id }
$emailId = Criar 'emails' @{ endereco = 'ana@example.com'; usuarioId = $clienteId.id }
$credencialId = Criar 'credenciais-senha' @{ nomeUsuario = "ana-$sufixo"; senha = 'ExemploAV3!'; usuarioId = $clienteId.id }
$codigoId = Criar 'credenciais-codigo' @{ codigo = [long]$sufixo; usuarioId = $clienteId.id }
$vendaId = Criar 'vendas' @{ identificacao = "VENDA-$sufixo"; empresaId = $empresaId.id; clienteId = $clienteId.id; funcionarioId = $funcionarioId.id; veiculoId = $veiculoId.id; mercadoriasIds = @($mercadoriaId.id); servicosIds = @($servicoId.id) }
Write-Host "Venda criada: $($vendaId.id)"
Invoke-RestMethod -Uri "$Base/vendas/$($vendaId.id)" | ConvertTo-Json -Depth 15
