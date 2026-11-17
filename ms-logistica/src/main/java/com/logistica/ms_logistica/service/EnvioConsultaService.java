package com.logistica.ms_logistica.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Asignacion;
import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EnvioPaquete;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.dto.EnvioDetalleResponse;
import com.logistica.ms_logistica.exception.RecursoNoEncontradoException;
import com.logistica.ms_logistica.repository.EnvioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnvioConsultaService {

    private final EnvioRepository envioRepository;

    @Transactional(readOnly = true)
    public List<EnvioDetalleResponse> buscar(EstadoEnvio estado, String sector) {
        String sectorNormalizado = normalizarSector(sector);
        return envioRepository.buscarConFiltros(estado, sectorNormalizado).stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnvioDetalleResponse obtener(Long envioId, String repartidorSub, boolean administrador) {
        validarId(envioId);
        Envio envio = envioRepository.buscarParaConsulta(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));

        if (!administrador) {
            Asignacion asignacion = envio.getAsignacion();
            if (repartidorSub == null || asignacion == null
                    || !Objects.equals(asignacion.getRepartidorSub(), repartidorSub)) {
                throw new AccessDeniedException("El envío no está asignado al repartidor autenticado");
            }
        }

        return mapear(envio);
    }

    private EnvioDetalleResponse mapear(Envio envio) {
        Asignacion asignacion = envio.getAsignacion();
        List<EnvioDetalleResponse.Paquete> paquetes = envio.getPaquetes().stream()
                .sorted(Comparator.comparing(EnvioPaquete::getPaqueteId))
                .map(paquete -> new EnvioDetalleResponse.Paquete(
                        paquete.getPaqueteId(),
                        paquete.getDireccionDestino(),
                        paquete.getLatitudDestino(),
                        paquete.getLongitudDestino(),
                        paquete.getResultado(),
                        paquete.getFechaIntento(),
                        paquete.getMotivoIncidencia()))
                .toList();

        return new EnvioDetalleResponse(
                envio.getId(),
                envio.getSucursalId(),
                envio.getSector(),
                envio.getEstado(),
                envio.getFechaCreacion(),
                envio.getFechaInicio(),
                envio.getFechaFin(),
                asignacion == null ? null : asignacion.getVehiculoId(),
                asignacion == null ? null : asignacion.getRepartidorId(),
                paquetes);
    }

    private String normalizarSector(String sector) {
        if (sector == null) {
            return null;
        }
        String valor = sector.strip();
        if (valor.isEmpty() || valor.length() > 20) {
            throw new IllegalArgumentException("El sector debe tener entre 1 y 20 caracteres");
        }
        return valor;
    }

    private void validarId(Long envioId) {
        if (envioId == null || envioId <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser positivo");
        }
    }
}
