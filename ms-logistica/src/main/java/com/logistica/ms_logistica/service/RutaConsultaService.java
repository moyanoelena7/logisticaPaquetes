package com.logistica.ms_logistica.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_logistica.domain.Envio;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.domain.ParadaRuta;
import com.logistica.ms_logistica.domain.RutaPlanificada;
import com.logistica.ms_logistica.dto.RutaResponse;
import com.logistica.ms_logistica.exception.RecursoNoEncontradoException;
import com.logistica.ms_logistica.repository.EnvioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RutaConsultaService {

    private static final List<EstadoEnvio> ESTADOS_CON_RUTA = List.of(
            EstadoEnvio.PLANIFICADO,
            EstadoEnvio.EN_CAMINO,
            EstadoEnvio.PENDIENTE_RETORNO,
            EstadoEnvio.CERRADO
    );

    private final EnvioRepository envioRepository;

    @Transactional(readOnly = true)
    public RutaResponse obtenerRuta(Long envioId, String repartidorSub, boolean administrador) {
        Envio envio = envioRepository.buscarConRuta(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el envío " + envioId));

        if (!administrador && (repartidorSub == null || envio.getAsignacion() == null
                || !Objects.equals(envio.getAsignacion().getRepartidorSub(), repartidorSub))) {
            throw new AccessDeniedException("El envío no está asignado al repartidor autenticado");
        }

        return mapearRuta(envio);
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> obtenerMisRutas(String repartidorSub) {
        if (repartidorSub == null || repartidorSub.isBlank()) {
            throw new AccessDeniedException("El token no contiene un subject de repartidor válido");
        }

        return envioRepository.buscarRutasDelRepartidor(repartidorSub, ESTADOS_CON_RUTA).stream()
                .map(this::mapearRuta)
                .toList();
    }

    private RutaResponse mapearRuta(Envio envio) {
        RutaPlanificada ruta = envio.getRuta();
        if (ruta == null) {
            throw new RecursoNoEncontradoException("El envío " + envio.getId() + " todavía no tiene ruta");
        }

        List<RutaResponse.Parada> paradas = ruta.getParadas().stream()
                .sorted(Comparator.comparingInt(ParadaRuta::getOrdenVisita))
                .map(parada -> new RutaResponse.Parada(
                        parada.getOrdenVisita(),
                        parada.getEnvioPaquete().getPaqueteId(),
                        parada.getEnvioPaquete().getDireccionDestino(),
                        parada.getEnvioPaquete().getLatitudDestino(),
                        parada.getEnvioPaquete().getLongitudDestino()))
                .toList();

        return new RutaResponse(
                envio.getId(),
                envio.getEstado(),
                ruta.getDistanciaMetros(),
                ruta.getDuracionSegundos(),
                ruta.getGeometriaGeojson(),
                ruta.getFechaCalculo(),
                paradas
        );
    }
}
