package com.banco.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> manejarApiException(
            ApiException ex) {

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }
}