package com.logistica.ms_paquetes.dto;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;

public record PaqueteLogisticaResponse(
        Long id,
        Long sucursalId,
        Sector sector,
        String estado
) {
    public static PaqueteLogisticaResponse from(Paquete paquete) {
        return new PaqueteLogisticaResponse(
                paquete.getId(),
                paquete.getSucursal().getId(),
                paquete.getSector(),
                paquete.getEstado().nombre()
        );
    }
}
