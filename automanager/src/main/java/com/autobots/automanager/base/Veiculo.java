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
public class Veiculo extends Registro {
 @Enumerated(EnumType.STRING)
 @NotNull private TipoVeiculo tipo;
 @NotBlank private String modelo;
 @Column(unique=true)
 @NotBlank
 @Pattern(regexp="[A-Z]{3}([0-9]{4}|[0-9][A-Z][0-9]{2})", message="placa deve usar ABC1234 ou ABC1D23")
 private String placa;
 @ManyToOne
 @NotNull
 private Usuario proprietario;
}
