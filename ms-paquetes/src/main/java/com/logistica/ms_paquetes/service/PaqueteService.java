package com.logistica.ms_paquetes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;
import com.logistica.ms_paquetes.domain.estado.EstadoPaqueteState;
import com.logistica.ms_paquetes.domain.estado.EstadoPaqueteStateFactory;
import com.logistica.ms_paquetes.dto.PaqueteRequest;
import com.logistica.ms_paquetes.dto.PaqueteResponse;
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
        paquete.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
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

    @Transactional
    public PaqueteResponse actualizar(Long id, PaqueteRequest req) {
        Paquete paquete = buscar(id);

        if (!paquete.getCliente().getId().equals(req.clienteId())) {
            paquete.setCliente(clienteService.buscar(req.clienteId()));
        }
        // Solo se valida la sucursal si el paquete cambia de sucursal.
        if (!paquete.getSucursal().getId().equals(req.sucursalId())) {
            paquete.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
        }

        aplicar(paquete, req);
        return PaqueteResponse.from(paquete);
    }

    /**
     * Cambia el estado del paquete validando la transición con el patrón State.
     * El estado destino no se aplica si el estado actual no lo permite.
     */
    @Transactional
    public PaqueteResponse cambiarEstado(Long id, EstadoPaquete nuevo) {
        Paquete paquete = buscar(id);

        // Mismo estado: no-op idempotente, igual que desactivar().
        if (paquete.getEstado() == nuevo) {
            return PaqueteResponse.from(paquete);
        }

        EstadoPaqueteState actual = EstadoPaqueteStateFactory.obtener(paquete.getEstado());
        if (!actual.permiteTransicionA(nuevo)) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el estado del paquete " + id
                            + " de " + paquete.getEstado() + " a " + nuevo);
        }

        paquete.setEstado(nuevo);
        return PaqueteResponse.from(paquete);
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