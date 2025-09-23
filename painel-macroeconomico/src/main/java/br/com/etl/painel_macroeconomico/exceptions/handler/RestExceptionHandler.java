package br.com.etl.painel_macroeconomico.exceptions.handler;


import br.com.etl.painel_macroeconomico.dto.ErrorMenssageDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.etl.painel_macroeconomico.exceptions.UserException;


@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    private ResponseEntity<String> handlerGenericException(Exception ex ) {
        return ResponseEntity.badRequest().body(ex.getMessage());    
    }
    
    @ExceptionHandler(UserException.class)
    private ResponseEntity<String> handlerIllegalEmailException(UserException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());    
    }

    //@ExceptionHandler(UserException.class)
    private ErrorMenssageDTO handler(ErrorMenssageDTO ex) {
        return new ErrorMenssageDTO(
                ex.getStatus(), "E-mail inválido! O email deve conter '@gmail'.", ex.getTimestamp(), ex.getPath()
        );
    }
}
