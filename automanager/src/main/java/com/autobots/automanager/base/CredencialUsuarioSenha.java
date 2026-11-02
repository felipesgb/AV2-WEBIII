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
public class CredencialUsuarioSenha extends Credencial {
 @Column(unique=true)
 @NotBlank private String nomeUsuario;
 @NotBlank private String senha;
 @ManyToOne
 @NotNull
 private Usuario usuario;
}
