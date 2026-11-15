package com.logistica.ms_paquetes.dto;

import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;

import jakarta.validation.constraints.NotNull;

public record EstadoPaqueteRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoPaquete estado
) { }