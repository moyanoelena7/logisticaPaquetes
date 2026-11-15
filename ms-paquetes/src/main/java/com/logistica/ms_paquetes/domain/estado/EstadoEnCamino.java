package com.logistica.ms_paquetes.domain.estado;

import java.util.Set;

final class EstadoEnCamino implements EstadoPaqueteState {

    @Override
    public Set<EstadoPaquete> transicionesPermitidas() {
        return Set.of(EstadoPaquete.ENTREGADO, EstadoPaquete.INCIDENCIA);
    }
}