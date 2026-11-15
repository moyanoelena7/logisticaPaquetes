package com.logistica.ms_paquetes.domain.estado;

import java.util.Set;

public interface EstadoPaqueteState {

    Set<EstadoPaquete> transicionesPermitidas();

    default boolean permiteTransicionA(EstadoPaquete destino) {
        return transicionesPermitidas().contains(destino);
    }
}