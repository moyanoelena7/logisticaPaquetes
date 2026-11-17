package com.logistica.ms_paquetes.service;

import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;
import com.logistica.ms_paquetes.dto.PaqueteRequest;
import com.logistica.ms_paquetes.dto.PaqueteResponse;
import com.logistica.ms_paquetes.dto.PaqueteLogisticaResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;
import com.logistica.ms_paquetes.repository.PaqueteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private final PaqueteRepository repository;
    private final ClienteService clienteService;
    private final SucursalService sucursalService;

    @Transactional
    public PaqueteResponse crear(PaqueteRequest req) {
        Paquete paquete = new Paquete();
        paquete.setCliente(clienteService.buscar(req.clienteId()));
        paquete.setSucursal(sucursalService.obtenerCentralAsignable(req.sucursalId()));
        // sector, estado y fechaRegistro los inicializa la entidad:
        // CENTRAL, PENDIENTE y el instante actual.
        aplicar(paquete, req);
        return PaqueteResponse.from(repository.save(paquete));
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponse> listar(EstadoPaquete estado, Sector sector) {
        List<Paquete> paquetes;
        if (estado != null && sector != null) {
            paquetes = repository.findByEstadoAndSector(estado, sector);
        } else if (estado != null) {
            paquetes = repository.findByEstado(estado);
        } else if (sector != null) {
            paquetes = repository.findBySector(sector);
        } else {
            paquetes = repository.findAll();
        }
        return paquetes.stream().map(PaqueteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PaqueteResponse obtener(Long id) {
        return PaqueteResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<PaqueteLogisticaResponse> obtenerParaLogistica(Collection<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 99) {
            throw new ReglaNegocioException("Debe indicar entre 1 y 99 IDs de paquetes");
        }
        Set<Long> idsUnicos = new HashSet<>(ids);
        if (idsUnicos.size() != ids.size()) {
            throw new ReglaNegocioException("La lista de paquetes no debe contener IDs duplicados");
        }

        List<Paquete> paquetes = repository.findAllById(idsUnicos);
        if (paquetes.size() != idsUnicos.size()) {
            Set<Long> encontrados = paquetes.stream().map(Paquete::getId)
                    .collect(java.util.stream.Collectors.toSet());
            Long faltante = ids.stream().filter(id -> !encontrados.contains(id)).findFirst().orElse(null);
            throw new RecursoNoEncontradoException("Paquete", faltante);
        }

        return paquetes.stream()
                .sorted(java.util.Comparator.comparing(Paquete::getId))
                .map(PaqueteLogisticaResponse::from)
                .toList();
    }

    @Transactional
    public PaqueteResponse actualizar(Long id, PaqueteRequest req) {
        Paquete paquete = repository.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));

        paquete.validarEdicion();

        if (!paquete.getCliente().getId().equals(req.clienteId())) {
            paquete.setCliente(clienteService.buscar(req.clienteId()));
        }
        // Solo se valida la sucursal si el paquete cambia de sucursal.
        if (!paquete.getSucursal().getId().equals(req.sucursalId())) {
            paquete.setSucursal(sucursalService.obtenerCentralAsignable(req.sucursalId()));
        } else {
            sucursalService.obtenerCentralAsignable(req.sucursalId());
        }

        aplicar(paquete, req);
        return PaqueteResponse.from(paquete);
    }

    /**
     * Cambia el estado del paquete validando la transición con el patrón State.
     * El estado destino no se aplica si el estado actual no lo permite.
     */
    @Transactional
    public PaqueteResponse cambiarEstado(Long id, String nuevo) {
        Paquete paquete = repository.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));

        // Mismo estado: no-op idempotente, igual que desactivar().
        if (paquete.getEstado().nombre().equals(nuevo)) {
            return PaqueteResponse.from(paquete);
        }

        // Adaptador del contrato HTTP: selecciona la operación solicitada.
        // El estado actual del paquete decide si esa operación es válida.
        switch (nuevo) {
            case "ASIGNADO" -> paquete.asignar();
            case "EN_CAMINO" -> paquete.enCamino();
            case "ENTREGADO" -> paquete.entregar();
            case "INCIDENCIA" -> paquete.registrarIncidencia();
            default -> throw new ReglaNegocioException(
                    "No se puede cambiar el estado del paquete " + id
                            + " de " + paquete.getEstado() + " a " + nuevo);
        }

        return PaqueteResponse.from(paquete);
    }

    @Transactional
    public PaqueteLogisticaResponse cambiarEstadoParaLogistica(Long id, String nuevo) {
        PaqueteResponse cambiado = cambiarEstado(id, nuevo);
        return obtenerParaLogistica(List.of(cambiado.id())).getFirst();
    }

    private Paquete buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));
    }

    private void aplicar(Paquete p, PaqueteRequest req) {
        p.setDireccionEnvio(req.direccionEnvio().trim());
        p.setLatitud(req.latitud());
        p.setLongitud(req.longitud());
        p.setAltoCm(req.altoCm());
        p.setAnchoCm(req.anchoCm());
        p.setLargoCm(req.largoCm());
        p.setPesoKg(req.pesoKg());
    }
}
