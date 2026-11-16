package com.logistica.ms_paquetes.dto;

import java.util.List;

import com.logistica.ms_paquetes.domain.Sector;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReservaEnvioRequest(
        @NotNull(message = "La sucursal es obligatoria")
        @Positive(message = "La sucursal debe ser un identificador positivo")
        Long sucursalId,

        @NotNull(message = "El sector es obligatorio")
        Sector sector,

        @NotNull(message = "La lista de paquetes es obligatoria")
        @Size(min = 6, max = 99, message = "La reserva debe incluir entre 6 y 99 paquetes")
        List<@NotNull @Positive(message = "Cada ID de paquete debe ser positivo") Long> paqueteIds,

        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El vehículo debe ser un identificador positivo")
        Long vehiculoId,

        @NotNull(message = "El repartidor es obligatorio")
        @Positive(message = "El repartidor debe ser un identificador positivo")
        Long repartidorId
) {
}
