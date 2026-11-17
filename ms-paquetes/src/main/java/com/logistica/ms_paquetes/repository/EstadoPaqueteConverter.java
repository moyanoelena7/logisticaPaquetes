package com.logistica.ms_paquetes.repository;

import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Conserva los nombres existentes en la columna paquete.estado. */
@Converter
public class EstadoPaqueteConverter implements AttributeConverter<EstadoPaquete, String> {
    @Override
    public String convertToDatabaseColumn(EstadoPaquete estado) {
        return estado == null ? null : estado.nombre();
    }

    @Override
    public EstadoPaquete convertToEntityAttribute(String nombre) {
        return nombre == null ? null : EstadoPaquete.desdeNombre(nombre);
    }
}
