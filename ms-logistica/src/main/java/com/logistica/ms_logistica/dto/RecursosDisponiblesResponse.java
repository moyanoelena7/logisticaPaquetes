package com.logistica.ms_logistica.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecursosDisponiblesResponse(
        List<Vehiculo> vehiculos,
        List<Repartidor> repartidores
) {
    public record Vehiculo(
            Long id,
            String patente,
            String modelo,
            BigDecimal capacidadPesoKg,
            BigDecimal capacidadVolumenM3,
            Long sucursalId
    ) {
    }

    public record Repartidor(
            Long id,
            String nombre,
            String username,
            Long sucursalId
    ) {
    }
}
