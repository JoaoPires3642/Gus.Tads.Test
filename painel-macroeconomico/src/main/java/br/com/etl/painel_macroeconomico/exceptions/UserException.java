package br.com.etl.painel_macroeconomico.exceptions;

public class UserException extends RuntimeException  {
    public UserException() {super("E-mail inválido! O email deve conter '@gmail'.");}
    public UserException(String message) {super(message);}
}
