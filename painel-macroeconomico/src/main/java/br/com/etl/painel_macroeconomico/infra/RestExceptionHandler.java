package br.com.etl.painel_macroeconomico.infra;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.etl.painel_macroeconomico.exceptions.RestErrorMenssage;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    private ResponseEntity<RestErrorMenssage> handlerGenericException(Exception ex ) {
        RestErrorMenssage errorMessage = new RestErrorMenssage(null, ex.getMessage(), null, null, null);
        return ResponseEntity.badRequest().body(errorMessage);  
        
    }
}
