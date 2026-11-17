package com.logistica.ms_paquetes.domain.estado;

import com.logistica.ms_paquetes.domain.Paquete;

public final class EstadoPendiente extends EstadoPaquete {
    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public EstadoPaquete asignar(Paquete paquete) {
        return new EstadoAsignado();
    }

    @Override
    public void validarEdicion() {
        // Los datos del paquete pendiente pueden editarse.
    }

    @Override
    public void validarReserva(Paquete paquete) {
        // El paquete pendiente puede incluirse en una reserva.
    }
}