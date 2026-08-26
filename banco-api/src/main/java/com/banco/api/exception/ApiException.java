package com.banco.api.exception;

public class ApiException extends RuntimeException {

    public ApiException(String mensaje) {
        super(mensaje);
    }
}