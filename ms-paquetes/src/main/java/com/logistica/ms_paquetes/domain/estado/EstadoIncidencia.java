package com.logistica.ms_paquetes.domain.estado;

import com.logistica.ms_paquetes.domain.Paquete;

public final class EstadoIncidencia extends EstadoPaquete {
    @Override
    public String nombre() {
        return "INCIDENCIA";
    }

    @Override
    public EstadoPaquete registrarIncidencia(Paquete paquete) {
        return this;
    }
}