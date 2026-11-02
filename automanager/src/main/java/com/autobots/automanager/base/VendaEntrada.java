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
public record VendaEntrada(
 String identificacao,
 LocalDate cadastro,
 Long empresaId,
 Long clienteId,
 Long funcionarioId,
 Long veiculoId,
 Set<Long> mercadoriasIds,
 Set<Long> servicosIds) {}
