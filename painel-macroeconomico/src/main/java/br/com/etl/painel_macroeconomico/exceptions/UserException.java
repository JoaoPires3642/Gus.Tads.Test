package br.com.etl.painel_macroeconomico.exceptions;

public class UserException extends RuntimeException {
    public UserException() {super("");}
    public UserException(String message) {super(message);}

}
