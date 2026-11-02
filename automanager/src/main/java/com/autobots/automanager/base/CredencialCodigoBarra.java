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
public class CredencialCodigoBarra extends Credencial {
 @Column(unique=true)
 @NotNull @Min(1) private Long codigo;
 @ManyToOne
 @NotNull
 private Usuario usuario;
}
