package com.logistica.ms_paquetes.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErrorResponse(Instant timestamp, int status, String error,
                            String mensaje, List<String> detalles) {

    public static ErrorResponse of(HttpStatus status, String mensaje, List<String> detalles) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                mensaje, detalles);
    }
}