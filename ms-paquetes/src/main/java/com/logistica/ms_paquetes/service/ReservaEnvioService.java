package com.logistica.ms_paquetes.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;
import com.logistica.ms_paquetes.domain.Repartidor;
import com.logistica.ms_paquetes.domain.ReservaEnvio;
import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.Sucursal;
import com.logistica.ms_paquetes.domain.Vehiculo;
import com.logistica.ms_paquetes.dto.RepartidorResponse;
import com.logistica.ms_paquetes.dto.ReservaEnvioRequest;
import com.logistica.ms_paquetes.dto.ReservaEnvioResponse;
import com.logistica.ms_paquetes.dto.VehiculoResponse;
import com.logistica.ms_paquetes.exception.ConflictoReservaException;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;
import com.logistica.ms_paquetes.repository.PaqueteRepository;
import com.logistica.ms_paquetes.repository.RepartidorRepository;
import com.logistica.ms_paquetes.repository.ReservaEnvioRepository;
import com.logistica.ms_paquetes.repository.ReservaEnvioMutexRepository;
import com.logistica.ms_paquetes.repository.VehiculoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaEnvioService {

    private static final BigDecimal CM3_POR_M3 = new BigDecimal("1000000");

    private final ReservaEnvioRepository reservaRepository;
    private final ReservaEnvioMutexRepository mutexRepository;
    private final PaqueteRepository paqueteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RepartidorRepository repartidorRepository;
    private final SucursalService sucursalService;
    private final PaqueteService paqueteService;

    @Transactional(readOnly = true)
    public List<VehiculoResponse> vehiculosDisponibles(Long sucursalId) {
        sucursalService.obtenerCentralAsignable(sucursalId);
        Set<Long> reservados = new HashSet<>(reservaRepository.findIdsVehiculosReservados());
        return vehiculoRepository.findBySucursal_IdAndDisponibleTrue(sucursalId).stream()
                .filter(vehiculo -> !reservados.contains(vehiculo.getId()))
                .map(VehiculoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RepartidorResponse> repartidoresDisponibles(Long sucursalId) {
        sucursalService.obtenerCentralAsignable(sucursalId);
        Set<Long> reservados = new HashSet<>(reservaRepository.findIdsRepartidoresReservados());
        return repartidorRepository.findBySucursal_IdAndDisponibleTrue(sucursalId).stream()
                .filter(repartidor -> repartidor.getKeycloakSub() != null
                        && !repartidor.getKeycloakSub().isBlank())
                .filter(repartidor -> !reservados.contains(repartidor.getId()))
                .map(RepartidorResponse::from)
                .toList();
    }

    @Transactional
    public ReservaEnvioResponse reservar(Long envioId, ReservaEnvioRequest req) {
        if (envioId == null || envioId <= 0) {
            throw new ReglaNegocioException("El ID del envío debe ser positivo");
        }
        List<Long> idsOrdenados = idsOrdenadosYUnicos(req.paqueteIds());

        bloquearEnvio(envioId);
        ReservaEnvio existente = reservaRepository.findByEnvioId(envioId).orElse(null);
        if (existente != null) {
            return responderSiEsElMismoRequest(existente, req, idsOrdenados);
        }

        List<Paquete> paquetes = paqueteRepository.bloquearPorIds(idsOrdenados);
        if (paquetes.size() != idsOrdenados.size()) {
            Set<Long> encontrados = paquetes.stream().map(Paquete::getId)
                    .collect(java.util.stream.Collectors.toSet());
            Long faltante = idsOrdenados.stream().filter(id -> !encontrados.contains(id)).findFirst().orElse(null);
            throw new RecursoNoEncontradoException("Paquete", faltante);
        }

        Vehiculo vehiculo = vehiculoRepository.bloquearPorId(req.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", req.vehiculoId()));
        Repartidor repartidor = repartidorRepository.bloquearPorId(req.repartidorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor", req.repartidorId()));
        Sucursal sucursal = sucursalService.obtenerCentralAsignable(req.sucursalId());

        existente = reservaRepository.findByEnvioId(envioId).orElse(null);
        if (existente != null) {
            return responderSiEsElMismoRequest(existente, req, idsOrdenados);
        }

        validarRecursos(sucursal, req.sector(), paquetes, vehiculo, repartidor);

        if (!reservaRepository.bloquearReservasActivasParaPaquetes(idsOrdenados).isEmpty()
                || !reservaRepository.bloquearReservasActivasParaVehiculo(vehiculo.getId()).isEmpty()
                || !reservaRepository.bloquearReservasActivasParaRepartidor(repartidor.getId()).isEmpty()) {
            throw new ConflictoReservaException(
                    "Uno o más paquetes, el vehículo o el repartidor ya tienen una reserva activa");
        }

        BigDecimal pesoTotal = paquetes.stream()
                .map(Paquete::getPesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal volumenTotal = paquetes.stream()
                .map(this::volumenM3)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (pesoTotal.compareTo(vehiculo.getCapacidadPesoKg()) > 0) {
            throw new ReglaNegocioException("El peso total de los paquetes supera la capacidad del vehículo");
        }
        if (volumenTotal.compareTo(vehiculo.getCapacidadVolumenM3()) > 0) {
            throw new ReglaNegocioException("El volumen total de los paquetes supera la capacidad del vehículo");
        }
        if (repartidor.getKeycloakSub() == null || repartidor.getKeycloakSub().isBlank()) {
            throw new ReglaNegocioException(
                    "El repartidor debe tener vinculado su subject estable de Keycloak antes de asignarlo");
        }

        ReservaEnvio reserva = new ReservaEnvio();
        reserva.setEnvioId(envioId);
        reserva.setSucursal(sucursal);
        reserva.setSector(req.sector());
        reserva.setVehiculo(vehiculo);
        reserva.setRepartidor(repartidor);
        reserva.setRepartidorSub(repartidor.getKeycloakSub());
        reserva.setPesoTotalKg(pesoTotal);
        reserva.setVolumenTotalM3(volumenTotal);
        for (Paquete paquete : paquetes) {
            reserva.agregarPaquete(paquete);
        }

        reservaRepository.save(reserva);
        for (Paquete paquete : paquetes) {
            paqueteService.cambiarEstado(paquete.getId(), EstadoPaquete.ASIGNADO);
        }

        return ReservaEnvioResponse.from(reserva);
    }

    @Transactional
    public void liberar(Long envioId) {
        if (envioId == null || envioId <= 0) {
            throw new ReglaNegocioException("El ID del envío debe ser positivo");
        }
        bloquearEnvio(envioId);
        ReservaEnvio reserva = reservaRepository.findByEnvioId(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de envío", envioId));
        if (!reserva.isActiva()) {
            return;
        }

        List<Long> paqueteIds = reserva.getPaquetes().stream()
                .map(rp -> rp.getPaquete().getId())
                .sorted()
                .toList();
        paqueteRepository.bloquearPorIds(paqueteIds);
        vehiculoRepository.bloquearPorId(reserva.getVehiculo().getId());
        repartidorRepository.bloquearPorId(reserva.getRepartidor().getId());

        ReservaEnvio bloqueada = reservaRepository.bloquearPorEnvioId(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de envío", envioId));
        bloqueada.liberar();
    }

    private void bloquearEnvio(Long envioId) {
        mutexRepository.asegurarFila(envioId);
        mutexRepository.bloquearPorEnvioId(envioId)
                .orElseThrow(() -> new IllegalStateException("No se pudo bloquear el envío " + envioId));
    }

    private List<Long> idsOrdenadosYUnicos(List<Long> ids) {
        if (ids == null || ids.size() < 6 || ids.size() > 99) {
            throw new ReglaNegocioException("La reserva debe incluir entre 6 y 99 paquetes");
        }
        List<Long> ordenados = ids.stream().sorted().toList();
        if (new HashSet<>(ordenados).size() != ordenados.size()) {
            throw new ReglaNegocioException("La lista de paquetes no debe contener IDs duplicados");
        }
        return ordenados;
    }

    private ReservaEnvioResponse responderSiEsElMismoRequest(
            ReservaEnvio reserva,
            ReservaEnvioRequest req,
            List<Long> idsOrdenados
    ) {
        List<Long> idsExistentes = reserva.getPaquetes().stream()
                .map(rp -> rp.getPaquete().getId())
                .sorted()
                .toList();
        boolean mismo = idsExistentes.equals(idsOrdenados)
                && reserva.getSucursal().getId().equals(req.sucursalId())
                && reserva.getSector() == req.sector()
                && reserva.getVehiculo().getId().equals(req.vehiculoId())
                && reserva.getRepartidor().getId().equals(req.repartidorId());
        if (!mismo) {
            throw new ConflictoReservaException(
                    "El envioId " + reserva.getEnvioId() + " ya tiene una reserva con datos distintos");
        }
        return ReservaEnvioResponse.from(reserva);
    }

    private void validarRecursos(
            Sucursal sucursal,
            Sector sector,
            List<Paquete> paquetes,
            Vehiculo vehiculo,
            Repartidor repartidor
    ) {
        if (!vehiculo.isDisponible()) {
            throw new ConflictoReservaException("El vehículo está deshabilitado administrativamente");
        }
        if (!repartidor.isDisponible()) {
            throw new ConflictoReservaException("El repartidor está deshabilitado administrativamente");
        }
        if (!vehiculo.getSucursal().getId().equals(sucursal.getId())) {
            throw new ReglaNegocioException("El vehículo no pertenece a la sucursal del envío");
        }
        if (!repartidor.getSucursal().getId().equals(sucursal.getId())) {
            throw new ReglaNegocioException("El repartidor no pertenece a la sucursal del envío");
        }
        for (Paquete paquete : paquetes) {
            if (!paquete.getSucursal().getId().equals(sucursal.getId())) {
                throw new ReglaNegocioException(
                        "El paquete " + paquete.getId() + " no pertenece a la sucursal indicada");
            }
            if (paquete.getSector() != sector) {
                throw new ReglaNegocioException(
                        "El paquete " + paquete.getId() + " no pertenece al sector indicado");
            }
            if (paquete.getEstado() != EstadoPaquete.PENDIENTE) {
                throw new ReglaNegocioException(
                        "El paquete " + paquete.getId() + " no está en estado PENDIENTE");
            }
        }
    }

    private BigDecimal volumenM3(Paquete paquete) {
        return paquete.getAltoCm()
                .multiply(paquete.getAnchoCm())
                .multiply(paquete.getLargoCm())
                .divide(CM3_POR_M3);
    }
}
