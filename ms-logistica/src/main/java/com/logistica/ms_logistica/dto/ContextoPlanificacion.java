package com.logistica.ms_logistica.dto;

import java.math.BigDecimal;
import java.util.List;

import com.logistica.ms_logistica.domain.EstadoEnvio;

public record ContextoPlanificacion(
        Long envioId,
        EstadoEnvio estado,
        BigDecimal latitudOrigen,
        BigDecimal longitudOrigen,
        List<PuntoEntrega> entregas
) {
    public record PuntoEntrega(
            Long paqueteId,
            BigDecimal latitud,
            BigDecimal longitud
    ) {
    }
}
