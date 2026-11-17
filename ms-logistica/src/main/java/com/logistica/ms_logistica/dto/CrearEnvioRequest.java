package com.logistica.ms_logistica.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearEnvioRequest(
        @NotNull(message = "La sucursal es obligatoria")
        @Positive(message = "La sucursal debe ser un ID positivo")
        Long sucursalId,

        @NotBlank(message = "El sector es obligatorio")
        @Size(max = 20, message = "El sector no puede superar los 20 caracteres")
        String sector,

        @NotNull(message = "La lista de paquetes es obligatoria")
        @Size(min = 6, max = 99, message = "El envío debe incluir entre 6 y 99 paquetes")
        List<@NotNull @Positive(message = "Cada ID de paquete debe ser positivo") Long> paqueteIds
) {
}
