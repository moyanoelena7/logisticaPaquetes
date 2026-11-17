package com.logistica.ms_paquetes.domain.estado;

import com.logistica.ms_paquetes.domain.Paquete;

public final class EstadoAsignado extends EstadoPaquete {
    @Override
    public String nombre() {
        return "ASIGNADO";
    }

    @Override
    public EstadoPaquete asignar(Paquete paquete) {
        return this;
    }

    @Override
    public EstadoPaquete enCamino(Paquete paquete) {
        return new EstadoEnCamino();
    }
}