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
public class Usuario extends Registro {
 @NotBlank private String nome;
  private String nomeSocial;
 @ElementCollection @Enumerated(EnumType.STRING)
 @NotEmpty private Set<PerfilUsuario> perfis = new LinkedHashSet<>();
 @ManyToOne
 @NotNull
 private Empresa empresa;
 @ManyToOne
 private Endereco endereco;
 @ManyToMany
 private Set<Telefone> telefones = new LinkedHashSet<>();
 @ManyToMany
 private Set<Documento> documentos = new LinkedHashSet<>();
}
