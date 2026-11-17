package com.logistica.ms_logistica.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.logistica.ms_logistica.domain.EstadoEnvio;

public record RutaResponse(
        Long envioId,
        EstadoEnvio estadoEnvio,
        BigDecimal distanciaMetros,
        BigDecimal duracionSegundos,
        String geometriaGeojson,
        Instant fechaCalculo,
        List<Parada> paradas
) {
    public record Parada(
            int orden,
            Long paqueteId,
            String direccion,
            BigDecimal latitud,
            BigDecimal longitud
    ) {
    }
}
