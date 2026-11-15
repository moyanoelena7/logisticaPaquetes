package com.logistica.ms_paquetes.domain.estado;

import java.util.Set;


final class EstadoAsignado implements EstadoPaqueteState {

    @Override
    public Set<EstadoPaquete> transicionesPermitidas() {
        return Set.of(EstadoPaquete.EN_CAMINO, EstadoPaquete.PENDIENTE);
    }
}