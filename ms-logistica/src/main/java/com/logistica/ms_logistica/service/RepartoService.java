package com.logistica.ms_logistica.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.logistica.ms_logistica.client.PaquetesClient;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ResultadoEntrega;
import com.logistica.ms_logistica.dto.ContextoReparto;
import com.logistica.ms_logistica.dto.RegistrarResultadoEntregaRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepartoService {

    private final EnvioService envioService;
    private final PaquetesClient paquetesClient;

    public void iniciar(Long envioId, String repartidorSub) {
        ContextoReparto contexto = envioService.obtenerContextoReparto(envioId);
        validarRepartidor(contexto, repartidorSub);

        if (contexto.estado() == EstadoEnvio.EN_CAMINO) {
            return;
        }
        if (contexto.estado() != EstadoEnvio.PLANIFICADO) {
            throw new IllegalStateException("Solo se puede iniciar un envío en estado PLANIFICADO");
        }
        if (!contexto.tieneRuta()) {
            throw new IllegalStateException("No se puede iniciar un envío sin una ruta planificada");
        }

        for (ContextoReparto.Paquete paquete : contexto.paquetes()) {
            if (paquete.resultado() == ResultadoEntrega.PENDIENTE) {
                paquetesClient.cambiarEstadoPaquete(paquete.paqueteId(), "EN_CAMINO");
            }
        }

        envioService.iniciarRepartoLocal(envioId, repartidorSub, Instant.now());
    }

    public void registrarResultado(
            Long envioId,
            Long paqueteId,
            String repartidorSub,
            RegistrarResultadoEntregaRequest request
    ) {
        if (request == null || request.resultado() == null) {
            throw new IllegalArgumentException("El resultado de la entrega es obligatorio");
        }
        if (request.resultado() != ResultadoEntrega.ENTREGADO
                && request.resultado() != ResultadoEntrega.INCIDENCIA) {
            throw new IllegalArgumentException("El resultado debe ser ENTREGADO o INCIDENCIA");
        }

        String motivo = normalizarMotivo(request.resultado(), request.motivoIncidencia());
        ContextoReparto contexto = envioService.obtenerContextoReparto(envioId);
        validarRepartidor(contexto, repartidorSub);

        ContextoReparto.Paquete paquete = contexto.paquetes().stream()
                .filter(item -> Objects.equals(item.paqueteId(), paqueteId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El paquete no pertenece al envío"));

        if (paquete.resultado() != ResultadoEntrega.PENDIENTE) {
            if (paquete.resultado() == request.resultado()
                    && Objects.equals(paquete.motivoIncidencia(), motivo)) {
                return;
            }
            throw new IllegalStateException("El paquete ya tiene un resultado distinto registrado para el envío");
        }
        if (contexto.estado() != EstadoEnvio.EN_CAMINO) {
            throw new IllegalStateException("Solo se pueden registrar entregas de un envío en estado EN_CAMINO");
        }

        String estadoPaquete = request.resultado() == ResultadoEntrega.ENTREGADO
                ? "ENTREGADO"
                : "INCIDENCIA";
        paquetesClient.cambiarEstadoPaquete(paqueteId, estadoPaquete);
        envioService.registrarResultadoLocal(
                envioId, paqueteId, repartidorSub, request.resultado(), motivo, Instant.now());
    }

    public void cerrar(Long envioId, String repartidorSub) {
        ContextoReparto contexto = envioService.obtenerContextoReparto(envioId);
        validarRepartidor(contexto, repartidorSub);

        if (contexto.estado() != EstadoEnvio.CERRADO
                && contexto.estado() != EstadoEnvio.PENDIENTE_RETORNO) {
            throw new IllegalStateException(
                    "El envío solo puede cerrarse después de registrar el resultado de todos sus paquetes");
        }

        // La liberación remota es idempotente. Si la respuesta se pierde, repetir el cierre
        // permite completar el cambio local sin dejar recursos reservados.
        paquetesClient.liberarReserva(envioId);
        if (contexto.estado() != EstadoEnvio.CERRADO) {
            envioService.cerrarRepartoLocal(envioId, repartidorSub, Instant.now());
        }
    }

    private String normalizarMotivo(ResultadoEntrega resultado, String motivoIncidencia) {
        if (resultado == ResultadoEntrega.INCIDENCIA) {
            if (motivoIncidencia == null || motivoIncidencia.isBlank()) {
                throw new IllegalArgumentException("El motivo es obligatorio para una incidencia");
            }
            return motivoIncidencia.strip();
        }
        if (motivoIncidencia != null && !motivoIncidencia.isBlank()) {
            throw new IllegalArgumentException("El motivo solo corresponde a un resultado INCIDENCIA");
        }
        return null;
    }

    private void validarRepartidor(ContextoReparto contexto, String repartidorSub) {
        if (repartidorSub == null || repartidorSub.isBlank()
                || contexto.repartidorSub() == null
                || !Objects.equals(contexto.repartidorSub(), repartidorSub)) {
            throw new AccessDeniedException("El envío no está asignado al repartidor autenticado");
        }
    }
}
