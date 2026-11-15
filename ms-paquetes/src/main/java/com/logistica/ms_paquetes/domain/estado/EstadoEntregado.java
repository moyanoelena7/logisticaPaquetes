package com.logistica.ms_paquetes.domain.estado;

import java.util.Set;
final class EstadoEntregado implements EstadoPaqueteState {

    @Override
    public Set<EstadoPaquete> transicionesPermitidas() {
        return Set.of();
    }
}