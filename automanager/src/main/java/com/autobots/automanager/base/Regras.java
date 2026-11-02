package com.autobots.automanager.base;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Regras {
    private Regras() {}
    private static void exigir(boolean ok,String mensagem) { if(!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,mensagem); }
    private static boolean perfil(Usuario u,PerfilUsuario p) { return u!=null && u.getPerfis()!=null && u.getPerfis().contains(p); }
    private static boolean mesma(Empresa a,Empresa b) { return a!=null && b!=null && Objects.equals(a.getId(),b.getId()); }
    static void validar(Object e) {
        if(e instanceof Mercadoria m) {
            exigir(m.getFabricacao()==null || m.getValidade()==null || !m.getValidade().isBefore(m.getFabricacao()),"Validade anterior a fabricacao");
            if(m.getFornecedor()!=null) exigir(perfil(m.getFornecedor(),PerfilUsuario.FORNECEDOR) && mesma(m.getEmpresa(),m.getFornecedor().getEmpresa()),"Fornecedor deve pertencer a empresa e ter perfil FORNECEDOR");
        }
        if(e instanceof Veiculo v) exigir(perfil(v.getProprietario(),PerfilUsuario.CLIENTE),"Proprietario deve ter perfil CLIENTE");
        if(e instanceof Venda v) {
            exigir(perfil(v.getCliente(),PerfilUsuario.CLIENTE),"Cliente deve ter perfil CLIENTE");
            exigir(perfil(v.getFuncionario(),PerfilUsuario.FUNCIONARIO),"Funcionario deve ter perfil FUNCIONARIO");
            exigir(mesma(v.getEmpresa(),v.getCliente().getEmpresa()) && mesma(v.getEmpresa(),v.getFuncionario().getEmpresa()),"Participantes devem pertencer a empresa da venda");
            if(v.getVeiculo()!=null) exigir(Objects.equals(v.getVeiculo().getProprietario().getId(),v.getCliente().getId()),"Veiculo deve pertencer ao cliente");
            exigir(!v.getMercadorias().isEmpty() || !v.getServicos().isEmpty(),"Venda deve conter mercadoria ou servico");
            for(var m:v.getMercadorias()) exigir(mesma(v.getEmpresa(),m.getEmpresa()),"Mercadoria de outra empresa");
            for(var s:v.getServicos()) exigir(mesma(v.getEmpresa(),s.getEmpresa()),"Servico de outra empresa");
        }
    }
}
