package com.logistica.ms_logistica.exception;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.logistica.ms_logistica.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(this::mensajeCampo)
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoInvalido(HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no tiene un formato válido", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarReglaDeEntrada(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> manejarConflictoDeEstado(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(RespuestaRemotaInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarRespuestaRemotaInvalida(
            RespuestaRemotaInvalidaException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(RespuestaOsrmInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarRespuestaOsrmInvalida(
            RespuestaOsrmInvalidaException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(CalculoRutaNoPosibleException.class)
    public ResponseEntity<ErrorResponse> manejarRutaNoPosible(
            CalculoRutaNoPosibleException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> manejarErrorRemoto(
            RestClientResponseException exception,
            HttpServletRequest request
    ) {
        int statusRemoto = exception.getStatusCode().value();
        HttpStatus destino = switch (statusRemoto) {
            case 404, 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            case 409 -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_GATEWAY;
        };
        String mensaje = destino == HttpStatus.BAD_GATEWAY
                ? "Una dependencia externa no pudo completar la solicitud"
                : "Una dependencia externa rechazó los datos de la solicitud";
        return responder(destino, mensaje, request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> manejarDependenciaInaccesible(
            ResourceAccessException exception,
            HttpServletRequest request
    ) {
        Throwable causa = NestedExceptionUtils.getMostSpecificCause(exception);
        boolean timeout = causa instanceof SocketTimeoutException || causa instanceof HttpTimeoutException;
        HttpStatus status = timeout ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE;
        String mensaje = timeout
                ? "Se agotó el tiempo de espera al consultar un servicio externo"
                : "No se pudo establecer conexión con un servicio externo";
        return responder(status, mensaje, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarNoAutenticado(HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Se requiere autenticación válida", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarSinPermiso(HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta operación", request);
    }

    private String mensajeCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> responder(
            HttpStatusCode status,
            String mensaje,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                HttpStatus.valueOf(status.value()).getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
