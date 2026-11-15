package com.logistica.ms_paquetes.dto;

import com.logistica.ms_paquetes.domain.Repartidor;

public record RepartidorResponse(
        Long id,
        String nombre,
        String telefono,
        String username,
        Long sucursalId,
        boolean disponible
) {
    public static RepartidorResponse from(Repartidor r) {
        return new RepartidorResponse(r.getId(), r.getNombre(), r.getTelefono(),
                r.getUsername(), r.getSucursal().getId(), r.isDisponible());
    }
}