package com.logistica.ms_paquetes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EstadoPaqueteRequest(
        @NotNull(message = "El estado es obligatorio")
        @Pattern(regexp = "PENDIENTE|ASIGNADO|EN_CAMINO|ENTREGADO|INCIDENCIA",
                message = "El estado no es válido")
        String estado
) { }
