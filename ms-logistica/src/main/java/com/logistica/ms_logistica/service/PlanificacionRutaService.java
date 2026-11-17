package com.logistica.ms_logistica.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EnvioPaquete;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ParadaRuta;
import com.logistica.ms_logistica.domain.RutaPlanificada;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanificacionRutaService {

    private final EnvioService envioService;

    @Transactional
    public void guardarRuta(Long envioId, BigDecimal distanciaMetros, BigDecimal duracionSegundos,
                            String geometriaGeojson, Instant fechaCalculo, List<Long> paqueteIdsEnOrden) {
        Envio envio = envioService.buscarParaActualizar(envioId);
        if (envio.getEstado() == EstadoEnvio.PLANIFICADO && envio.getRuta() != null) {
            return;
        }
        if (envio.getEstado() != EstadoEnvio.ASIGNADO) {
            throw new IllegalStateException("La ruta solo se puede guardar para un envío ASIGNADO");
        }
        Map<Long, EnvioPaquete> paquetesPorId = envio.getPaquetes().stream()
                .collect(Collectors.toMap(EnvioPaquete::getPaqueteId, Function.identity()));
        if (paqueteIdsEnOrden == null || paqueteIdsEnOrden.size() != paquetesPorId.size()
                || !paquetesPorId.keySet().equals(new HashSet<>(paqueteIdsEnOrden))) {
            throw new IllegalArgumentException("La ruta debe visitar una vez cada paquete del envío");
        }

        RutaPlanificada ruta = new RutaPlanificada(envio, Objects.requireNonNull(distanciaMetros),
                Objects.requireNonNull(duracionSegundos), Objects.requireNonNull(geometriaGeojson),
                Objects.requireNonNull(fechaCalculo));
        int ordenVisita = 1;
        for (Long paqueteId : paqueteIdsEnOrden) {
            ruta.getParadas().add(new ParadaRuta(ruta, paquetesPorId.get(paqueteId), ordenVisita++));
        }
        envio.setRuta(ruta);
        envio.setEstado(EstadoEnvio.PLANIFICADO);
    }
}
