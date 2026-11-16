package com.logistica.ms_logistica.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record AsignacionConfirmada(
        Long vehiculoId,
        Long repartidorId,
        String repartidorSub,
        BigDecimal pesoTotalKg,
        BigDecimal volumenTotalM3,
        BigDecimal latitudSalida,
        BigDecimal longitudSalida,
        Instant fechaAsignacion,
        Map<Long, Destino> destinos
) {
    public record Destino(String direccion, BigDecimal latitud, BigDecimal longitud) {
    }
}
