package com.logistica.ms_logistica.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Asignacion;
import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EnvioPaquete;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;
import com.logistica.ms_logistica.dto.AsignacionConfirmada;
import com.logistica.ms_logistica.dto.ContextoAsignacion;
import com.logistica.ms_logistica.dto.ContextoPlanificacion;
import com.logistica.ms_logistica.dto.ContextoReparto;
import com.logistica.ms_logistica.exception.RecursoNoEncontradoException;
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

    @Transactional(readOnly = true)
    public ContextoAsignacion obtenerContextoAsignacion(Long envioId) {
        Envio envio = repository.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));
        EstadoEnvio estado = envio.getEstado();
        if (estado != EstadoEnvio.CREADO && !asignacionPuedeReintentarse(estado)) {
            throw new IllegalStateException("El envío no admite asignación en estado " + estado);
        }

        Asignacion asignacion = envio.getAsignacion();
        if (estado != EstadoEnvio.CREADO && asignacion == null) {
            throw new IllegalStateException("El envío tiene un estado de asignación sin asignación guardada");
        }

        List<Long> paqueteIds = envio.getPaquetes().stream()
                .map(EnvioPaquete::getPaqueteId)
                .toList();
        return new ContextoAsignacion(
                envio.getId(),
                envio.getSucursalId(),
                envio.getSector(),
                estado,
                paqueteIds,
                asignacion == null ? null : asignacion.getVehiculoId(),
                asignacion == null ? null : asignacion.getRepartidorId()
        );
    }

    @Transactional(readOnly = true)
    public ContextoPlanificacion obtenerContextoPlanificacion(Long envioId) {
        Envio envio = repository.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));
        if (envio.getEstado() != EstadoEnvio.ASIGNADO && envio.getEstado() != EstadoEnvio.PLANIFICADO) {
            throw new IllegalStateException("El envío debe estar ASIGNADO para calcular la ruta");
        }

        Asignacion asignacion = envio.getAsignacion();
        if (asignacion == null) {
            throw new IllegalStateException("El envío no tiene una asignación confirmada");
        }

        List<ContextoPlanificacion.PuntoEntrega> entregas = envio.getPaquetes().stream()
                .map(paquete -> {
                    if (paquete.getLatitudDestino() == null || paquete.getLongitudDestino() == null) {
                        throw new IllegalStateException(
                                "Faltan las coordenadas del paquete " + paquete.getPaqueteId());
                    }
                    return new ContextoPlanificacion.PuntoEntrega(
                            paquete.getPaqueteId(),
                            paquete.getLatitudDestino(),
                            paquete.getLongitudDestino());
                })
                .sorted(Comparator.comparing(ContextoPlanificacion.PuntoEntrega::paqueteId))
                .toList();

        if (entregas.isEmpty() || asignacion.getLatitudSalida() == null
                || asignacion.getLongitudSalida() == null) {
            throw new IllegalStateException("El envío no tiene origen o destinos para calcular la ruta");
        }

        return new ContextoPlanificacion(
                envio.getId(),
                envio.getEstado(),
                asignacion.getLatitudSalida(),
                asignacion.getLongitudSalida(),
                entregas
        );
    }

    @Transactional(readOnly = true)
    public ContextoReparto obtenerContextoReparto(Long envioId) {
        validarEnvioId(envioId);
        Envio envio = repository.buscarContextoReparto(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));
        Asignacion asignacion = envio.getAsignacion();
        return new ContextoReparto(
                envio.getId(),
                envio.getEstado(),
                asignacion == null ? null : asignacion.getRepartidorSub(),
                envio.getRuta() != null,
                envio.getPaquetes().stream()
                        .map(paquete -> new ContextoReparto.Paquete(
                                paquete.getPaqueteId(),
                                paquete.getResultado(),
                                paquete.getMotivoIncidencia()))
                        .toList());
    }

    @Transactional
    public void iniciarRepartoLocal(Long envioId, String repartidorSub, Instant fechaInicio) {
        Envio envio = buscarParaActualizar(envioId);
        validarRepartidorAsignado(envio, repartidorSub);

        if (envio.getEstado() == EstadoEnvio.EN_CAMINO) {
            return;
        }
        if (envio.getEstado() != EstadoEnvio.PLANIFICADO) {
            throw new IllegalStateException("Solo se puede iniciar un envío en estado PLANIFICADO");
        }
        if (envio.getAsignacion() == null || envio.getRuta() == null) {
            throw new IllegalStateException("El envío requiere una asignación y una ruta planificada para iniciar");
        }

        envio.setFechaInicio(Objects.requireNonNull(fechaInicio));
        envio.setEstado(EstadoEnvio.EN_CAMINO);
    }

    @Transactional
    public void registrarResultadoLocal(
            Long envioId,
            Long paqueteId,
            String repartidorSub,
            ResultadoEntrega resultado,
            String motivoIncidencia,
            Instant fechaIntento
    ) {
        Envio envio = buscarParaActualizar(envioId);
        validarRepartidorAsignado(envio, repartidorSub);
        EnvioPaquete paquete = envio.getPaquetes().stream()
                .filter(item -> Objects.equals(item.getPaqueteId(), paqueteId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El paquete no pertenece al envío"));

        if (paquete.getResultado() != ResultadoEntrega.PENDIENTE) {
            if (paquete.getResultado() == resultado
                    && Objects.equals(paquete.getMotivoIncidencia(), motivoIncidencia)) {
                return;
            }
            throw new IllegalStateException("El paquete ya tiene un resultado distinto registrado para el envío");
        }
        if (envio.getEstado() != EstadoEnvio.EN_CAMINO) {
            throw new IllegalStateException("Solo se pueden registrar entregas de un envío en estado EN_CAMINO");
        }

        paquete.setFechaIntento(Objects.requireNonNull(fechaIntento));
        paquete.setResultado(Objects.requireNonNull(resultado));
        paquete.setMotivoIncidencia(motivoIncidencia);
        if (envio.getPaquetes().stream().noneMatch(item -> item.getResultado() == ResultadoEntrega.PENDIENTE)) {
            envio.setEstado(EstadoEnvio.PENDIENTE_RETORNO);
        }
    }

    @Transactional
    public void cerrarRepartoLocal(Long envioId, String repartidorSub, Instant fechaFin) {
        Envio envio = buscarParaActualizar(envioId);
        validarRepartidorAsignado(envio, repartidorSub);

        if (envio.getEstado() == EstadoEnvio.CERRADO) {
            return;
        }
        if (envio.getEstado() != EstadoEnvio.PENDIENTE_RETORNO) {
            throw new IllegalStateException("El envío debe estar en estado PENDIENTE_RETORNO para cerrarse");
        }

        envio.setFechaFin(Objects.requireNonNull(fechaFin));
        envio.setEstado(EstadoEnvio.CERRADO);
    }

    private void validarRepartidorAsignado(Envio envio, String repartidorSub) {
        if (repartidorSub == null || repartidorSub.isBlank()
                || envio.getAsignacion() == null
                || !Objects.equals(envio.getAsignacion().getRepartidorSub(), repartidorSub)) {
            throw new AccessDeniedException("El envío no está asignado al repartidor autenticado");
        }
    }

    private void validarEnvioId(Long envioId) {
        if (envioId == null || envioId <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser positivo");
        }
    }

    @Transactional
    public void confirmarAsignacion(Long envioId, AsignacionConfirmada datos) {
        Envio envio = buscarParaActualizar(envioId);
        validarRespuestaReserva(envio, envioId, datos);

        if (envio.getEstado() == EstadoEnvio.CREADO && !datos.activa()) {
            throw new IllegalStateException("La reserva de ms-paquetes ya no está activa");
        }

        if (envio.getEstado() != EstadoEnvio.CREADO) {
            if (asignacionPuedeReintentarse(envio.getEstado())
                    && datos.activa() == (envio.getEstado() != EstadoEnvio.CERRADO)
                    && asignacionCoincide(envio, datos)) {
                return;
            }
            throw new IllegalStateException(
                    "El envío ya tiene una asignación distinta o no admite confirmar una asignación");
        }

        guardarAsignacion(envio, datos);
    }

    private void validarRespuestaReserva(Envio envio, Long envioId, AsignacionConfirmada datos) {
        if (datos == null || !Objects.equals(datos.envioId(), envioId)) {
            throw new IllegalArgumentException("La reserva remota no corresponde al envío solicitado");
        }
        if (datos.repartidorSub() == null || datos.repartidorSub().isBlank()) {
            throw new IllegalArgumentException("El repartidor debe estar vinculado a una identidad de Keycloak");
        }

        Set<Long> paqueteIds = envio.getPaquetes().stream()
                .map(EnvioPaquete::getPaqueteId)
                .collect(Collectors.toSet());
        if (datos.paqueteIds() == null || new HashSet<>(datos.paqueteIds()).size() != datos.paqueteIds().size()
                || !paqueteIds.equals(new HashSet<>(datos.paqueteIds()))
                || datos.destinos() == null || !paqueteIds.equals(datos.destinos().keySet())) {
            throw new IllegalArgumentException("La asignación debe incluir el destino de cada paquete del envío");
        }
    }

    private void guardarAsignacion(Envio envio, AsignacionConfirmada datos) {
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

    private boolean asignacionPuedeReintentarse(EstadoEnvio estado) {
        return estado == EstadoEnvio.ASIGNADO
                || estado == EstadoEnvio.PLANIFICADO
                || estado == EstadoEnvio.EN_CAMINO
                || estado == EstadoEnvio.PENDIENTE_RETORNO
                || estado == EstadoEnvio.CERRADO;
    }

    private boolean asignacionCoincide(Envio envio, AsignacionConfirmada datos) {
        Asignacion existente = envio.getAsignacion();
        if (existente == null
                || !Objects.equals(existente.getVehiculoId(), datos.vehiculoId())
                || !Objects.equals(existente.getRepartidorId(), datos.repartidorId())
                || !Objects.equals(existente.getRepartidorSub(), datos.repartidorSub())
                || !mismoDecimal(existente.getPesoTotalKg(), datos.pesoTotalKg())
                || !mismoDecimal(existente.getVolumenTotalM3(), datos.volumenTotalM3())
                || !mismoDecimal(existente.getLatitudSalida(), datos.latitudSalida())
                || !mismoDecimal(existente.getLongitudSalida(), datos.longitudSalida())
                || !Objects.equals(existente.getFechaAsignacion(), datos.fechaAsignacion())) {
            return false;
        }

        for (EnvioPaquete paquete : envio.getPaquetes()) {
            AsignacionConfirmada.Destino destino = datos.destinos().get(paquete.getPaqueteId());
            if (destino == null
                    || !Objects.equals(paquete.getDireccionDestino(), destino.direccion())
                    || !mismoDecimal(paquete.getLatitudDestino(), destino.latitud())
                    || !mismoDecimal(paquete.getLongitudDestino(), destino.longitud())) {
                return false;
            }
        }
        return true;
    }

    private boolean mismoDecimal(BigDecimal primero, BigDecimal segundo) {
        return primero != null && segundo != null && primero.compareTo(segundo) == 0;
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

    Envio buscarParaActualizar(Long envioId) {
        return repository.buscarParaActualizar(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));
    }
}
