package com.logistica.ms_logistica.dto;

import java.util.List;

import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;

public record ContextoReparto(
        Long envioId,
        EstadoEnvio estado,
        String repartidorSub,
        boolean tieneRuta,
        List<Paquete> paquetes
) {
    public record Paquete(
            Long paqueteId,
            ResultadoEntrega resultado,
            String motivoIncidencia
    ) {
    }
}
