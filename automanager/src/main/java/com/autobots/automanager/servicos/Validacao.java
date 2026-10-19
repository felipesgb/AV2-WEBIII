package com.autobots.automanager.servicos;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class Validacao {
    private final Validator validator;
    public Validacao(Validator validator) { this.validator = validator; }
    public void validar(Object objeto) {
        var erros = validator.validate(objeto);
        if (!erros.isEmpty()) throw new ConstraintViolationException(erros);
    }
    public void novo(Long id) {
        if (id != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao informe ID no cadastro");
    }
    public void mesmoId(Long id, Long corpo) {
        if (corpo != null && !corpo.equals(id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do corpo diferente da URL");
    }
}
