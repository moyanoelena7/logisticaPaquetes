package com.logistica.ms_logistica.dto;

import java.util.List;

import com.logistica.ms_logistica.domain.EstadoEnvio;

public record ContextoAsignacion(
        Long envioId,
        Long sucursalId,
        String sector,
        EstadoEnvio estado,
        List<Long> paqueteIds,
        Long vehiculoId,
        Long repartidorId
) {
}
