package com.logistica.ms_paquetes.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.logistica.ms_paquetes.domain.Sucursal;
import com.logistica.ms_paquetes.dto.SucursalRequest;
import com.logistica.ms_paquetes.dto.SucursalResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;
import com.logistica.ms_paquetes.repository.SucursalRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository repository;

    @Transactional
    public SucursalResponse crear(SucursalRequest req) {
        Sucursal sucursal = new Sucursal();
        aplicar(sucursal, req);
        if (Boolean.TRUE.equals(req.esCentral())) {
            desmarcarCentralesExistentes();
        }
        return SucursalResponse.from(repository.save(sucursal));
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar() {
        return repository.findAll().stream().map(SucursalResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtener(Long id) {
        return SucursalResponse.from(buscar(id));
    }

    @Transactional
    public SucursalResponse actualizar(Long id, SucursalRequest req) {
        Sucursal sucursal = buscar(id);
        if (sucursal.isEsCentral() && !Boolean.TRUE.equals(req.esCentral())) {
            throw new ReglaNegocioException(
                    "Para cambiar la sucursal central, marque otra sucursal como central primero");
        }
        if (Boolean.TRUE.equals(req.esCentral())) {
            desmarcarCentralesExistentes();
        }
        aplicar(sucursal, req);
        return SucursalResponse.from(sucursal);
    }

    @Transactional
    public SucursalResponse desactivar(Long id) {
        Sucursal sucursal = buscar(id);
        if (sucursal.isEsCentral()) {
            throw new ReglaNegocioException("No se puede desactivar la sucursal central");
        }
        sucursal.setActivo(false);   // idempotente: desactivar dos veces no es un error
        return SucursalResponse.from(sucursal);
    }

    /** Lo usarán Vehículo, Repartidor y Paquete para validar su sucursal. */
    @Transactional(readOnly = true)
    public Sucursal buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
    }

    private void aplicar(Sucursal s, SucursalRequest req) {
        s.setNombre(req.nombre().trim());
        s.setDireccion(req.direccion().trim());
        s.setLatitud(req.latitud());
        s.setLongitud(req.longitud());
        s.setEsCentral(Boolean.TRUE.equals(req.esCentral()));
    }
    /** Para entidades que referencian una sucursal desde el cuerpo de la petición: 422 si no sirve. */
    @Transactional(readOnly = true)
    public Sucursal obtenerAsignable(Long sucursalId) {
        Sucursal sucursal = repository.findById(sucursalId)
                .orElseThrow(() -> new ReglaNegocioException(
                        "La sucursal con id " + sucursalId + " no existe"));
        if (!sucursal.isActivo()) {
            throw new ReglaNegocioException("La sucursal con id " + sucursalId + " está desactivada");
        }
        return sucursal;
    }

    /** Comprueba que el origen indicado sea la sucursal central operativa. */
    @Transactional(readOnly = true)
    public Sucursal obtenerCentralAsignable(Long sucursalId) {
        Sucursal sucursal = obtenerAsignable(sucursalId);
        if (!sucursal.isEsCentral()) {
            throw new ReglaNegocioException(
                    "La sucursal con id " + sucursalId + " no está marcada como central");
        }
        return sucursal;
    }

    private void desmarcarCentralesExistentes() {
        repository.findAll().stream()
                .filter(Sucursal::isEsCentral)
                .forEach(sucursal -> sucursal.setEsCentral(false));
    }
}
