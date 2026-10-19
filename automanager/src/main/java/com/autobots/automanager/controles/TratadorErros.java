package com.autobots.automanager.controles;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorErros {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException erro) {
        return resposta(erro.getStatusCode(), erro.getReason());
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> validar(ConstraintViolationException erro) {
        String mensagem = erro.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().collect(java.util.stream.Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflito(DataIntegrityViolationException erro) {
        return resposta(HttpStatus.CONFLICT, "Conflito de integridade: verifique numeros de documentos duplicados e vinculos");
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> formato(Exception erro) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON, data ou parametro invalido");
    }
    private ResponseEntity<?> resposta(org.springframework.http.HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "mensagem", mensagem == null ? HttpStatus.valueOf(status.value()).getReasonPhrase() : mensagem));
    }
}
