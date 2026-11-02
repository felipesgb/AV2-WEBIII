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
public class Mercadoria extends Registro {
 @NotBlank private String nome;
  private String descricao;
 @NotNull @DecimalMin("0.00") private BigDecimal valor;
 @NotNull @Min(0) private Long quantidade;
 @NotNull private LocalDate validade;
 @NotNull private LocalDate fabricacao;
 @NotNull private LocalDate cadastro = LocalDate.now();
 @ManyToOne
 @NotNull
 private Empresa empresa;
 @ManyToOne
 private Usuario fornecedor;
}
