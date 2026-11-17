package com.logistica.ms_paquetes.domain.estado;

import java.io.Serializable;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;

/**
 * Estado inmutable del paquete. Cada operación devuelve el estado resultante;
 * únicamente Paquete reemplaza su referencia, sin exponer un setter de estado.
 */
public abstract class EstadoPaquete implements Serializable {

    public abstract String nombre();

    public EstadoPaquete asignar(Paquete paquete) {
        throw transicionInvalida(paquete, "ASIGNADO");
    }

    public EstadoPaquete enCamino(Paquete paquete) {
        throw transicionInvalida(paquete, "EN_CAMINO");
    }

    public EstadoPaquete entregar(Paquete paquete) {
        throw transicionInvalida(paquete, "ENTREGADO");
    }

    public EstadoPaquete registrarIncidencia(Paquete paquete) {
        throw transicionInvalida(paquete, "INCIDENCIA");
    }

    public void validarEdicion() {
        throw new ReglaNegocioException(
                "Solo se pueden modificar los datos de un paquete en estado PENDIENTE");
    }

    public void validarReserva(Paquete paquete) {
        throw new ReglaNegocioException(
                "El paquete " + paquete.getId() + " no está en estado PENDIENTE");
    }

    private ReglaNegocioException transicionInvalida(Paquete paquete, String destino) {
        return new ReglaNegocioException("No se puede cambiar el estado del paquete "
                + paquete.getId() + " de " + nombre() + " a " + destino);
    }

    /** Conversión de representación; no decide ni ejecuta transiciones. */
    public static EstadoPaquete desdeNombre(String nombre) {
        return switch (nombre) {
            case "PENDIENTE" -> new EstadoPendiente();
            case "ASIGNADO" -> new EstadoAsignado();
            case "EN_CAMINO" -> new EstadoEnCamino();
            case "ENTREGADO" -> new EstadoEntregado();
            case "INCIDENCIA" -> new EstadoIncidencia();
            default -> throw new IllegalArgumentException("Estado de paquete inválido: " + nombre);
        };
    }

    @Override
    public final boolean equals(Object otro) {
        return otro != null && getClass() == otro.getClass();
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public final String toString() {
        return nombre();
    }
}
