package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;

public record OrigenReservaResponse(
        Long sucursalId,
        String nombre,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud
) {
}
