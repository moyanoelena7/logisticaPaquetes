package com.logistica.ms_logistica.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Asignacion;
import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EnvioPaquete;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;
import com.logistica.ms_logistica.dto.AsignacionConfirmada;
import com.logistica.ms_logistica.repository.EnvioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository repository;

    @Transactional
    public Long crearBorrador(Long sucursalId, String sector, List<Long> paqueteIds) {
        if (sucursalId == null || sucursalId <= 0) {
            throw new IllegalArgumentException("La sucursal debe tener un ID válido");
        }
        if (sector == null || sector.isBlank() || sector.length() > 20) {
            throw new IllegalArgumentException("El sector debe tener entre 1 y 20 caracteres");
        }
        if (paqueteIds == null || paqueteIds.size() < 6 || paqueteIds.size() > 99) {
            throw new IllegalArgumentException("El envío debe incluir entre 6 y 99 paquetes");
        }
        if (paqueteIds.stream().anyMatch(paqueteId -> paqueteId == null || paqueteId <= 0)
                || new HashSet<>(paqueteIds).size() != paqueteIds.size()) {
            throw new IllegalArgumentException("El envío debe incluir IDs de paquetes válidos y distintos");
        }

        Envio envio = new Envio(sucursalId, sector, EstadoEnvio.CREADO, Instant.now());
        for (Long paqueteId : paqueteIds) {
            envio.getPaquetes().add(new EnvioPaquete(envio, paqueteId, ResultadoEntrega.PENDIENTE));
        }
        return repository.save(envio).getId();
    }

    @Transactional
    public void confirmarAsignacion(Long envioId, AsignacionConfirmada datos) {
        Envio envio = buscarParaActualizar(envioId, EstadoEnvio.CREADO);
        if (datos.repartidorSub() == null || datos.repartidorSub().isBlank()) {
            throw new IllegalArgumentException("El repartidor debe estar vinculado a una identidad de Keycloak");
        }

        Set<Long> paqueteIds = envio.getPaquetes().stream()
                .map(EnvioPaquete::getPaqueteId)
                .collect(Collectors.toSet());
        if (datos.destinos() == null || !paqueteIds.equals(datos.destinos().keySet())) {
            throw new IllegalArgumentException("La asignación debe incluir el destino de cada paquete del envío");
        }

        Asignacion asignacion = new Asignacion(envio, Objects.requireNonNull(datos.vehiculoId()),
                Objects.requireNonNull(datos.repartidorId()), datos.repartidorSub(),
                Objects.requireNonNull(datos.pesoTotalKg()), Objects.requireNonNull(datos.volumenTotalM3()),
                Objects.requireNonNull(datos.latitudSalida()), Objects.requireNonNull(datos.longitudSalida()),
                Objects.requireNonNull(datos.fechaAsignacion()));
        for (EnvioPaquete paquete : envio.getPaquetes()) {
            AsignacionConfirmada.Destino destino = datos.destinos().get(paquete.getPaqueteId());
            if (destino == null || destino.direccion() == null || destino.direccion().isBlank()
                    || destino.latitud() == null || destino.longitud() == null) {
                throw new IllegalArgumentException("El destino requiere una dirección y coordenadas");
            }
            paquete.setDireccionDestino(destino.direccion());
            paquete.setLatitudDestino(destino.latitud());
            paquete.setLongitudDestino(destino.longitud());
        }
        envio.setAsignacion(asignacion);
        envio.setEstado(EstadoEnvio.ASIGNADO);
    }

    @Transactional
    public void cancelar(Long envioId, Instant fechaFin) {
        Envio envio = buscarParaActualizar(envioId);
        EstadoEnvio estado = envio.getEstado();
        if (estado != EstadoEnvio.CREADO && estado != EstadoEnvio.ASIGNADO && estado != EstadoEnvio.PLANIFICADO) {
            throw new IllegalStateException("Solo se puede cancelar un envío antes de iniciar");
        }
        envio.setFechaFin(Objects.requireNonNull(fechaFin));
        envio.setEstado(EstadoEnvio.CANCELADO);
    }

    Envio buscarParaActualizar(Long envioId, EstadoEnvio estadoRequerido) {
        Envio envio = buscarParaActualizar(envioId);
        if (envio.getEstado() != estadoRequerido) {
            throw new IllegalStateException("La operación requiere un envío en estado " + estadoRequerido);
        }
        return envio;
    }

    private Envio buscarParaActualizar(Long envioId) {
        return repository.buscarParaActualizar(envioId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el envío " + envioId));
    }
}
