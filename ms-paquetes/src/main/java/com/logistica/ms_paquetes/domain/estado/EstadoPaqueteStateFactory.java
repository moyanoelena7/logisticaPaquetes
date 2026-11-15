package com.logistica.ms_paquetes.domain.estado;

import java.util.Map;

public final class EstadoPaqueteStateFactory {

    private static final Map<EstadoPaquete, EstadoPaqueteState> ESTADOS = Map.of(
            EstadoPaquete.PENDIENTE, new EstadoPendiente(),
            EstadoPaquete.ASIGNADO, new EstadoAsignado(),
            EstadoPaquete.EN_CAMINO, new EstadoEnCamino(),
            EstadoPaquete.ENTREGADO, new EstadoEntregado(),
            EstadoPaquete.INCIDENCIA, new EstadoIncidencia()
    );

    private EstadoPaqueteStateFactory() {
    }

    public static EstadoPaqueteState obtener(EstadoPaquete estado) {
        EstadoPaqueteState state = ESTADOS.get(estado);
        if (state == null) {
            throw new IllegalStateException(
                    "No hay comportamiento definido para el estado " + estado);
        }
        return state;
    }
}