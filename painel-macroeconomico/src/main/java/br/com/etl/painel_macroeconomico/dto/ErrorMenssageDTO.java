package br.com.etl.painel_macroeconomico.dto;


import java.time.LocalDateTime;


import org.springframework.http.HttpStatus;

public class ErrorMenssageDTO {
    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private String error;

    public ErrorMenssageDTO(HttpStatus status, String message, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }


}
