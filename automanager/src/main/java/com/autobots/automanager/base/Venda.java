package com.autobots.automanager.base;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Documento;
@Getter @Setter @Entity
public class Venda extends Registro {
 @Column(unique=true)
 @NotBlank private String identificacao;
 @NotNull private LocalDate cadastro = LocalDate.now();
 @ManyToOne
 @NotNull
 private Empresa empresa;
 @ManyToOne
 @NotNull
 private Usuario cliente;
 @ManyToOne
 @NotNull
 private Usuario funcionario;
 @ManyToOne
 private Veiculo veiculo;
 @ManyToMany
 private Set<Mercadoria> mercadorias = new LinkedHashSet<>();
 @ManyToMany
 private Set<Servico> servicos = new LinkedHashSet<>();
}
