package com.logistica.ms_paquetes.dto;

import jakarta.validation.constraints.NotNull;

public record DisponibilidadRequest(
        @NotNull(message = "El campo disponible es obligatorio")
        Boolean disponible
) { }