package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;

public record PaqueteResponse(
        Long id,
        Long clienteId,
        Long sucursalId,
        Sector sector,
        String direccionEnvio,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        BigDecimal pesoKg,
        String estado,
        Instant fechaRegistro
) {
    public static PaqueteResponse from(Paquete p) {
        return new PaqueteResponse(
                p.getId(),
                p.getCliente().getId(),
                p.getSucursal().getId(),
                p.getSector(),
                p.getDireccionEnvio(),
                p.getLatitud(),
                p.getLongitud(),
                p.getAltoCm(),
                p.getAnchoCm(),
                p.getLargoCm(),
                p.getPesoKg(),
                p.getEstado().nombre(),
                p.getFechaRegistro()
        );
    }
}
