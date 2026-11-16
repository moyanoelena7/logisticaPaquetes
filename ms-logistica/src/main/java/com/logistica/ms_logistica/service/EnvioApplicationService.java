package com.logistica.ms_logistica.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import tools.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import com.logistica.ms_logistica.client.OsrmClient;
import com.logistica.ms_logistica.client.OsrmClient.Trip;
import com.logistica.ms_logistica.client.OsrmClient.TripResponse;
import com.logistica.ms_logistica.client.OsrmClient.Waypoint;
import com.logistica.ms_logistica.client.PaquetesClient;
import com.logistica.ms_logistica.client.PaquetesClient.PaqueteResumen;
import com.logistica.ms_logistica.client.PaquetesClient.RepartidorDisponible;
import com.logistica.ms_logistica.client.PaquetesClient.ReservaRequest;
import com.logistica.ms_logistica.client.PaquetesClient.ReservaResponse;
import com.logistica.ms_logistica.client.PaquetesClient.Destino;
import com.logistica.ms_logistica.client.PaquetesClient.VehiculoDisponible;
import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.dto.AsignacionConfirmada;
import com.logistica.ms_logistica.dto.AsignacionRequest;
import com.logistica.ms_logistica.dto.ContextoAsignacion;
import com.logistica.ms_logistica.dto.ContextoPlanificacion;
import com.logistica.ms_logistica.dto.CrearEnvioRequest;
import com.logistica.ms_logistica.dto.RecursosDisponiblesResponse;
import com.logistica.ms_logistica.dto.RutaResponse;
import com.logistica.ms_logistica.exception.RespuestaRemotaInvalidaException;


import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EnvioApplicationService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final PaquetesClient paquetesClient;
    private final OsrmClient osrmClient;
    private final EnvioService envioService;
    private final PlanificacionRutaService planificacionRutaService;
    private final RutaConsultaService rutaConsultaService;

    public Long crearBorrador(CrearEnvioRequest request) {
        validarRequest(request);

        String sector = request.sector().strip().toUpperCase(Locale.ROOT);
        List<PaqueteResumen> paquetes = paquetesClient.obtenerPaquetes(request.paqueteIds());
        validarPaquetesRemotos(request, sector, paquetes);

        // EnvioService abre una transacción local después de que terminó la llamada HTTP.
        return envioService.crearBorrador(request.sucursalId(), sector, request.paqueteIds());
    }

    public RecursosDisponiblesResponse obtenerRecursosDisponibles(Long sucursalId) {
        if (sucursalId == null || sucursalId <= 0) {
            throw new IllegalArgumentException("La sucursal debe tener un ID positivo");
        }

        List<VehiculoDisponible> vehiculos = paquetesClient.obtenerVehiculosDisponibles(sucursalId);
        List<RepartidorDisponible> repartidores = paquetesClient.obtenerRepartidoresDisponibles(sucursalId);
        if (vehiculos == null || repartidores == null
                || vehiculos.stream().anyMatch(v -> v == null || v.id() == null || v.id() <= 0
                        || v.patente() == null || v.patente().isBlank()
                        || v.capacidadPesoKg() == null || v.capacidadPesoKg().signum() <= 0
                        || v.capacidadVolumenM3() == null || v.capacidadVolumenM3().signum() <= 0
                        || !Objects.equals(v.sucursalId(), sucursalId) || !v.disponible())
                || repartidores.stream().anyMatch(r -> r == null || r.id() == null || r.id() <= 0
                        || r.nombre() == null || r.nombre().isBlank()
                        || !Objects.equals(r.sucursalId(), sucursalId) || !r.disponible()
                        || r.keycloakSub() == null || r.keycloakSub().isBlank())) {
            throw new RespuestaRemotaInvalidaException(
                    "ms-paquetes devolvió recursos disponibles incompletos o inconsistentes");
        }

        List<RecursosDisponiblesResponse.Vehiculo> vehiculosResponse = vehiculos.stream()
                .map(v -> new RecursosDisponiblesResponse.Vehiculo(
                        v.id(), v.patente(), v.modelo(), v.capacidadPesoKg(),
                        v.capacidadVolumenM3(), v.sucursalId()))
                .toList();
        List<RecursosDisponiblesResponse.Repartidor> repartidoresResponse = repartidores.stream()
                .map(r -> new RecursosDisponiblesResponse.Repartidor(
                        r.id(), r.nombre(), r.username(), r.sucursalId()))
                .toList();
        return new RecursosDisponiblesResponse(vehiculosResponse, repartidoresResponse);
    }

    public void asignar(Long envioId, AsignacionRequest request) {
        if (envioId == null || envioId <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser positivo");
        }
        if (request == null || request.vehiculoId() == null || request.vehiculoId() <= 0
                || request.repartidorId() == null || request.repartidorId() <= 0) {
            throw new IllegalArgumentException("Vehículo y repartidor deben tener IDs positivos");
        }

        ContextoAsignacion contexto = envioService.obtenerContextoAsignacion(envioId);
        if (contexto.estado() != EstadoEnvio.CREADO
                && (!Objects.equals(contexto.vehiculoId(), request.vehiculoId())
                || !Objects.equals(contexto.repartidorId(), request.repartidorId()))) {
            throw new IllegalStateException("El envío ya está asignado a otros recursos");
        }

        ReservaResponse reserva = paquetesClient.reservar(envioId, new ReservaRequest(
                contexto.sucursalId(), contexto.sector(), contexto.paqueteIds(),
                request.vehiculoId(), request.repartidorId()));
        envioService.confirmarAsignacion(envioId,
                convertirYValidarReserva(envioId, contexto, request, reserva));
    }

    public RutaResponse planificarRuta(Long envioId) {
        if (envioId == null || envioId <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser positivo");
        }
        ContextoPlanificacion contexto = envioService.obtenerContextoPlanificacion(envioId);
        if (contexto.estado() == EstadoEnvio.PLANIFICADO) {
            return rutaConsultaService.obtenerRuta(envioId, null, true);
        }

        validarCoordenada(contexto.latitudOrigen(), contexto.longitudOrigen());
        contexto.entregas().forEach(entrega -> validarCoordenada(entrega.latitud(), entrega.longitud()));
        ResultadoRuta resultado = validarYMapearRespuesta(contexto,
                osrmClient.calcularViaje(formarCoordenadas(contexto)));
        planificacionRutaService.guardarRuta(envioId, resultado.distanciaMetros(),
                resultado.duracionSegundos(), resultado.geometriaGeojson(), java.time.Instant.now(),
                resultado.paqueteIdsEnOrden());
        return rutaConsultaService.obtenerRuta(envioId, null, true);
    }

    private String formarCoordenadas(ContextoPlanificacion contexto) {
        StringJoiner coordenadas = new StringJoiner(";");
        coordenadas.add(coordenada(contexto.longitudOrigen(), contexto.latitudOrigen()));
        contexto.entregas().forEach(entrega -> coordenadas.add(
                coordenada(entrega.longitud(), entrega.latitud())));
        return coordenadas.toString();
    }

    private String coordenada(java.math.BigDecimal longitud, java.math.BigDecimal latitud) {
        return longitud.stripTrailingZeros().toPlainString() + ","
                + latitud.stripTrailingZeros().toPlainString();
    }

    private void validarCoordenada(java.math.BigDecimal latitud, java.math.BigDecimal longitud) {
        if (latitud == null || longitud == null
                || latitud.abs().compareTo(java.math.BigDecimal.valueOf(90)) > 0
                || longitud.abs().compareTo(java.math.BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("El origen o un destino tiene coordenadas inválidas");
        }
    }

    private ResultadoRuta validarYMapearRespuesta(ContextoPlanificacion contexto, TripResponse respuesta) {
        int cantidadPuntos = contexto.entregas().size() + 1;
        if (respuesta == null || !"Ok".equals(respuesta.code())
                || respuesta.trips() == null || respuesta.trips().size() != 1
                || respuesta.waypoints() == null || respuesta.waypoints().size() != cantidadPuntos) {
            throw new RespuestaRemotaInvalidaException("OSRM devolvió una respuesta de ruta inválida");
        }

        Trip viaje = respuesta.trips().getFirst();
        if (viaje == null || viaje.distance() == null || viaje.distance().signum() < 0
                || viaje.duration() == null || viaje.duration().signum() < 0
                || !geometriaValida(viaje.geometry())) {
            throw new RespuestaRemotaInvalidaException(
                    "OSRM devolvió distancia, duración o geometría inválidas");
        }

        Map<Integer, Long> paquetePorIndiceRuta = new HashMap<>();
        Set<Integer> indicesVistos = new HashSet<>();
        for (int indiceEntrada = 0; indiceEntrada < cantidadPuntos; indiceEntrada++) {
            Waypoint waypoint = respuesta.waypoints().get(indiceEntrada);
            if (waypoint == null || waypoint.tripsIndex() == null || waypoint.tripsIndex() != 0
                    || waypoint.waypointIndex() == null || waypoint.waypointIndex() < 0
                    || waypoint.waypointIndex() >= cantidadPuntos
                    || !indicesVistos.add(waypoint.waypointIndex())) {
                throw new RespuestaRemotaInvalidaException("OSRM devolvió índices de paradas inválidos");
            }
            if (indiceEntrada == 0) {
                if (waypoint.waypointIndex() != 0) {
                    throw new RespuestaRemotaInvalidaException(
                            "OSRM no mantuvo la sucursal como origen de la ruta");
                }
            } else {
                paquetePorIndiceRuta.put(waypoint.waypointIndex(),
                        contexto.entregas().get(indiceEntrada - 1).paqueteId());
            }
        }

        List<Long> ordenPaquetes = java.util.stream.IntStream.range(1, cantidadPuntos)
                .mapToObj(paquetePorIndiceRuta::get).toList();
        if (indicesVistos.size() != cantidadPuntos
                || ordenPaquetes.size() != contexto.entregas().size() || ordenPaquetes.contains(null)) {
            throw new RespuestaRemotaInvalidaException("OSRM devolvió un orden incompleto de paradas");
        }
        return new ResultadoRuta(viaje.distance(), viaje.duration(), viaje.geometry().toString(), ordenPaquetes);
    }

    private boolean geometriaValida(JsonNode geometria) {
        if (geometria == null || !geometria.isObject()
                || !"LineString".equals(geometria.path("type").asText())) {
            return false;
        }
        JsonNode coordenadas = geometria.path("coordinates");
        if (!coordenadas.isArray() || coordenadas.size() < 2) {
            return false;
        }
        for (JsonNode punto : coordenadas) {
            if (!punto.isArray() || punto.size() < 2
                    || !punto.get(0).isNumber() || !punto.get(1).isNumber()) {
                return false;
            }
        }
        return true;
    }

    private record ResultadoRuta(java.math.BigDecimal distanciaMetros,
                                 java.math.BigDecimal duracionSegundos,
                                 String geometriaGeojson,
                                 List<Long> paqueteIdsEnOrden) {
    }

    private AsignacionConfirmada convertirYValidarReserva(Long envioId, ContextoAsignacion contexto,
                                                           AsignacionRequest request, ReservaResponse reserva) {
        if (reserva == null || !Objects.equals(reserva.envioId(), envioId)
                || !Objects.equals(reserva.vehiculoId(), request.vehiculoId())
                || !Objects.equals(reserva.repartidorId(), request.repartidorId())
                || reserva.origen() == null
                || !Objects.equals(reserva.origen().sucursalId(), contexto.sucursalId())
                || reserva.repartidorSub() == null || reserva.repartidorSub().isBlank()
                || reserva.fechaReserva() == null || reserva.pesoTotalKg() == null
                || reserva.volumenTotalM3() == null || reserva.origen().latitud() == null
                || reserva.origen().longitud() == null) {
            throw new RespuestaRemotaInvalidaException(
                    "ms-paquetes devolvió datos de reserva incompletos o inconsistentes");
        }

        Set<Long> idsEsperados = Set.copyOf(contexto.paqueteIds());
        if (reserva.paqueteIds() == null || reserva.paqueteIds().size() != idsEsperados.size()
                || !idsEsperados.equals(new HashSet<>(reserva.paqueteIds()))
                || reserva.destinos() == null || reserva.destinos().size() != idsEsperados.size()) {
            throw new RespuestaRemotaInvalidaException(
                    "ms-paquetes devolvió una composición de paquetes inválida");
        }

        Map<Long, AsignacionConfirmada.Destino> destinos = new HashMap<>();
        for (Destino destino : reserva.destinos()) {
            if (destino == null || destino.paqueteId() == null || destino.direccionEnvio() == null
                    || destino.direccionEnvio().isBlank() || destino.latitud() == null || destino.longitud() == null
                    || destinos.put(destino.paqueteId(), new AsignacionConfirmada.Destino(
                            destino.direccionEnvio(), destino.latitud(), destino.longitud())) != null) {
                throw new RespuestaRemotaInvalidaException(
                        "ms-paquetes devolvió destinos inválidos o duplicados");
            }
        }
        if (!destinos.keySet().equals(idsEsperados)) {
            throw new RespuestaRemotaInvalidaException(
                    "ms-paquetes devolvió destinos distintos de los paquetes reservados");
        }

        boolean envioCerrado = contexto.estado() == EstadoEnvio.CERRADO;
        if (reserva.activa() == envioCerrado) {
            throw new IllegalStateException("La reserva remota no está activa para el estado actual del envío");
        }
        return new AsignacionConfirmada(reserva.envioId(), reserva.activa(), reserva.paqueteIds(),
                reserva.vehiculoId(), reserva.repartidorId(), reserva.repartidorSub(), reserva.pesoTotalKg(),
                reserva.volumenTotalM3(), reserva.origen().latitud(), reserva.origen().longitud(),
                reserva.fechaReserva(), destinos);
    }

    private void validarRequest(CrearEnvioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de creación del envío es obligatorio");
        }
        if (request.sucursalId() == null || request.sucursalId() <= 0) {
            throw new IllegalArgumentException("La sucursal debe tener un ID positivo");
        }
        if (request.sector() == null || request.sector().isBlank() || request.sector().length() > 20) {
            throw new IllegalArgumentException("El sector es obligatorio y no puede superar los 20 caracteres");
        }
        List<Long> ids = request.paqueteIds();
        if (ids == null || ids.size() < 6 || ids.size() > 99) {
            throw new IllegalArgumentException("El envío debe incluir entre 6 y 99 paquetes");
        }
        if (ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("El envío debe incluir IDs de paquetes positivos y distintos");
        }
    }

    private void validarPaquetesRemotos(
            CrearEnvioRequest request,
            String sector,
            List<PaqueteResumen> paquetes
    ) {
        Set<Long> idsSolicitados = new HashSet<>(request.paqueteIds());
        if (paquetes == null || paquetes.size() != idsSolicitados.size()) {
            throw new IllegalStateException("ms-paquetes devolvió una respuesta incompleta para los IDs solicitados");
        }

        Map<Long, PaqueteResumen> porId = new HashMap<>();
        for (PaqueteResumen paquete : paquetes) {
            if (paquete == null || paquete.id() == null || porId.put(paquete.id(), paquete) != null) {
                throw new IllegalStateException("ms-paquetes devolvió IDs de paquetes inválidos o duplicados");
            }
        }
        if (!porId.keySet().equals(idsSolicitados)) {
            throw new IllegalStateException("ms-paquetes devolvió IDs distintos de los solicitados");
        }

        for (PaqueteResumen paquete : porId.values()) {
            if (!Objects.equals(paquete.sucursalId(), request.sucursalId())) {
                throw new IllegalArgumentException(
                        "Todos los paquetes deben pertenecer a la sucursal indicada");
            }
            if (paquete.sector() == null || !paquete.sector().equalsIgnoreCase(sector)) {
                throw new IllegalArgumentException(
                        "Todos los paquetes deben pertenecer al sector indicado");
            }
            if (!ESTADO_PENDIENTE.equals(paquete.estado())) {
                throw new IllegalArgumentException(
                        "Todos los paquetes deben estar en estado PENDIENTE para crear el envío");
            }
        }
    }

   
}
