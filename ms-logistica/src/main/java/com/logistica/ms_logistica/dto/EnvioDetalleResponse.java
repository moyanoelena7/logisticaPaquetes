package com.logistica.ms_logistica.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;

public record EnvioDetalleResponse(
        Long id,
        Long sucursalId,
        String sector,
        EstadoEnvio estado,
        Instant fechaCreacion,
        Instant fechaInicio,
        Instant fechaFin,
        Long vehiculoId,
        Long repartidorId,
        List<Paquete> paquetes
) {
    public record Paquete(
            Long paqueteId,
            String direccionDestino,
            BigDecimal latitudDestino,
            BigDecimal longitudDestino,
            ResultadoEntrega resultado,
            Instant fechaIntento,
            String motivoIncidencia
    ) {
    }
}
