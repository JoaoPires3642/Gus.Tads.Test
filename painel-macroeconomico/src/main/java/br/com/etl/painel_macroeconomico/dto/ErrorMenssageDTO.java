package br.com.etl.painel_macroeconomico.dto;

import java.security.Timestamp;

import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;

public class ErrorMenssageDTO {
    private Http status;
    private String message;
    private Timestamp timestamp;
    private String path;
    private String error;

    public ErrorMenssageDTO(Http status, String message, Timestamp timestamp, String path) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public Http getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }


}
