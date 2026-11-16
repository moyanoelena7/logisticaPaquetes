package com.logistica.ms_logistica.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EnvioPaquete;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepartoService {

    private final EnvioService envioService;

    @Transactional
    public void iniciar(Long envioId, Instant fechaInicio) {
        Envio envio = envioService.buscarParaActualizar(envioId, EstadoEnvio.PLANIFICADO);
        envio.setFechaInicio(Objects.requireNonNull(fechaInicio));
        envio.setEstado(EstadoEnvio.EN_CAMINO);
    }

    @Transactional
    public void registrarResultado(Long envioId, Long paqueteId, ResultadoEntrega resultado,
                                   String motivoIncidencia, Instant fechaIntento) {
        Envio envio = envioService.buscarParaActualizar(envioId, EstadoEnvio.EN_CAMINO);
        EnvioPaquete paquete = envio.getPaquetes().stream()
                .filter(item -> item.getPaqueteId().equals(paqueteId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El paquete no pertenece al envío"));
        if (paquete.getResultado() != ResultadoEntrega.PENDIENTE) {
            throw new IllegalStateException("El paquete ya tiene un resultado registrado para el envío");
        }
        if (resultado != ResultadoEntrega.ENTREGADO && resultado != ResultadoEntrega.INCIDENCIA) {
            throw new IllegalArgumentException("El resultado debe ser ENTREGADO o INCIDENCIA");
        }
        if (resultado == ResultadoEntrega.INCIDENCIA && (motivoIncidencia == null || motivoIncidencia.isBlank())) {
            throw new IllegalArgumentException("El motivo es obligatorio para una incidencia");
        }

        paquete.setFechaIntento(Objects.requireNonNull(fechaIntento));
        paquete.setResultado(resultado);
        paquete.setMotivoIncidencia(resultado == ResultadoEntrega.INCIDENCIA ? motivoIncidencia.strip() : null);
        if (envio.getPaquetes().stream().noneMatch(item -> item.getResultado() == ResultadoEntrega.PENDIENTE)) {
            envio.setEstado(EstadoEnvio.PENDIENTE_RETORNO);
        }
    }

    @Transactional
    public void cerrar(Long envioId, Instant fechaFin) {
        Envio envio = envioService.buscarParaActualizar(envioId, EstadoEnvio.PENDIENTE_RETORNO);
        envio.setFechaFin(Objects.requireNonNull(fechaFin));
        envio.setEstado(EstadoEnvio.CERRADO);
    }
}
