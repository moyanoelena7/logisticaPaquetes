package com.logistica.ms_paquetes.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;

/** Mantiene el filtro GET /paquetes?estado=... */
@Component
public class EstadoPaqueteWebConverter implements Converter<String, EstadoPaquete> {
    @Override
    public EstadoPaquete convert(String source) {
        return source.isEmpty() ? null : EstadoPaquete.desdeNombre(source.trim());
    }
}
