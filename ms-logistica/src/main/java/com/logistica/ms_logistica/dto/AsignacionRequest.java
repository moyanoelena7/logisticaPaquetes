package com.logistica.ms_logistica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignacionRequest(
        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El vehículo debe ser un ID positivo")
        Long vehiculoId,

        @NotNull(message = "El repartidor es obligatorio")
        @Positive(message = "El repartidor debe ser un ID positivo")
        Long repartidorId
) {
}
