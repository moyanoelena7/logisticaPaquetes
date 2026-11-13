package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;

import com.logistica.ms_paquetes.domain.Vehiculo;

public record VehiculoResponse(
        Long id,
        String patente,
        String modelo,
        BigDecimal capacidadPesoKg,
        BigDecimal capacidadVolumenM3,
        Long sucursalId,
        boolean disponible
) {
    public static VehiculoResponse from(Vehiculo v) {
        return new VehiculoResponse(v.getId(), v.getPatente(), v.getModelo(),
                v.getCapacidadPesoKg(), v.getCapacidadVolumenM3(),
                v.getSucursal().getId(), v.isDisponible());
    }
}