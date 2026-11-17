package com.logistica.ms_paquetes.domain.estado;

import com.logistica.ms_paquetes.domain.Paquete;

public final class EstadoEnCamino extends EstadoPaquete {
    @Override
    public String nombre() {
        return "EN_CAMINO";
    }

    @Override
    public EstadoPaquete enCamino(Paquete paquete) {
        return this;
    }

    @Override
    public EstadoPaquete entregar(Paquete paquete) {
        return new EstadoEntregado();
    }

    @Override
    public EstadoPaquete registrarIncidencia(Paquete paquete) {
        return new EstadoIncidencia();
    }
}