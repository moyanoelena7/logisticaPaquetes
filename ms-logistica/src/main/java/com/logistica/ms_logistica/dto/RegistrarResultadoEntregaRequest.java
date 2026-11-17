package com.logistica.ms_logistica.dto;

import com.logistica.ms_logistica.domain.ResultadoEntrega;

import jakarta.validation.constraints.NotNull;

public record RegistrarResultadoEntregaRequest(
        @NotNull(message = "El resultado de la entrega es obligatorio")
        ResultadoEntrega resultado,
        String motivoIncidencia
) {
}
