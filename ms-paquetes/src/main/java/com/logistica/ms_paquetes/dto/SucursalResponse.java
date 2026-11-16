package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;

import com.logistica.ms_paquetes.domain.Sucursal;

public record SucursalResponse(
        Long id,
        String nombre,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        boolean activo,
        boolean esCentral
) {
    public static SucursalResponse from(Sucursal s) {
        return new SucursalResponse(s.getId(), s.getNombre(), s.getDireccion(),
                s.getLatitud(), s.getLongitud(), s.isActivo(), s.isEsCentral());
    }
}
