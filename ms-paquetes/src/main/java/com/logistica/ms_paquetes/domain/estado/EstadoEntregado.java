package com.logistica.ms_paquetes.domain.estado;

import com.logistica.ms_paquetes.domain.Paquete;

public final class EstadoEntregado extends EstadoPaquete {
    @Override
    public String nombre() {
        return "ENTREGADO";
    }

    @Override
    public EstadoPaquete entregar(Paquete paquete) {
        return this;
    }
}