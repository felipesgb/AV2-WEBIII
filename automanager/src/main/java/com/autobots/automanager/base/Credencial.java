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
@Getter @Setter @Entity @Inheritance(strategy=InheritanceType.JOINED)
public abstract class Credencial extends Registro {
 @NotNull private LocalDate criacao = LocalDate.now();
 private LocalDate ultimoAcesso;
 @NotNull private Boolean inativo = false;
}
