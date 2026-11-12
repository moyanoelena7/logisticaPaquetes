package com.logistica.ms_paquetes.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.logistica.ms_paquetes.domain.Sucursal;
import com.logistica.ms_paquetes.dto.SucursalRequest;
import com.logistica.ms_paquetes.dto.SucursalResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
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
        aplicar(sucursal, req);
        return SucursalResponse.from(sucursal);
    }

    @Transactional
    public SucursalResponse desactivar(Long id) {
        Sucursal sucursal = buscar(id);
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
    }
}