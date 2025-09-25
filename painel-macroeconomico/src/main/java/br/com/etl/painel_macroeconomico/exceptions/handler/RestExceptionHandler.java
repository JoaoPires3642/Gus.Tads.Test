package br.com.etl.painel_macroeconomico.exceptions.handler;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.etl.painel_macroeconomico.dto.ErrorMenssageDTO;
import br.com.etl.painel_macroeconomico.exceptions.UserException;
import jakarta.servlet.http.HttpServletRequest;


@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorMenssageDTO> handlerUserException(UserException ex, HttpServletRequest request) {

        ErrorMenssageDTO error = new ErrorMenssageDTO(
            HttpStatus.BAD_REQUEST, 
            ex.getMessage(),
            request.getRequestURI()
            );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);    
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMenssageDTO> handlerGenericException(Exception ex, HttpServletRequest request){
        ErrorMenssageDTO error = new ErrorMenssageDTO(
            HttpStatus.INTERNAL_SERVER_ERROR, 
        "Ocorreu um erro inesperado", 
        request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
}
