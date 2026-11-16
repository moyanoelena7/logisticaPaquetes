package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;

public record DestinoReservaResponse(
        Long paqueteId,
        String direccionEnvio,
        BigDecimal latitud,
        BigDecimal longitud
) {
}
