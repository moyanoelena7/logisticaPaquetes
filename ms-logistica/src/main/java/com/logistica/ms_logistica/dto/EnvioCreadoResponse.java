package com.logistica.ms_logistica.dto;

import com.logistica.ms_logistica.domain.EstadoEnvio;

public record EnvioCreadoResponse(
        Long id,
        EstadoEnvio estado
) {
}
