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
public class Empresa extends Registro {
 @NotBlank private String razaoSocial;
  private String nomeFantasia;
 @NotNull private LocalDate cadastro = LocalDate.now();
 @ManyToOne
 private Endereco endereco;
 @ManyToMany
 private Set<Telefone> telefones = new LinkedHashSet<>();
}
