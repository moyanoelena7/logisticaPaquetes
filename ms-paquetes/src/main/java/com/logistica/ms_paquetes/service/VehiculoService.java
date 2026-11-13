package com.logistica.ms_paquetes.service;

import java.util.List;

import org.springframework.stereotype.Service;


import com.logistica.ms_paquetes.domain.Vehiculo;
import com.logistica.ms_paquetes.dto.VehiculoRequest;
import com.logistica.ms_paquetes.dto.VehiculoResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;
import com.logistica.ms_paquetes.repository.VehiculoRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository repository;
    private final SucursalService sucursalService;

    @Transactional
    public VehiculoResponse crear(VehiculoRequest req) {
        String patente = normalizar(req.patente());
        if (repository.existsByPatenteIgnoreCase(patente)) {
            throw new ReglaNegocioException("Ya existe un vehículo con la patente " + patente);
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
        aplicar(vehiculo, req, patente);
        return VehiculoResponse.from(repository.save(vehiculo));
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponse> listar(Boolean disponible) {
        List<Vehiculo> vehiculos = (disponible == null)
                ? repository.findAll()
                : repository.findByDisponible(disponible);
        return vehiculos.stream().map(VehiculoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtener(Long id) {
        return VehiculoResponse.from(buscar(id));
    }

    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest req) {
        Vehiculo vehiculo = buscar(id);
        String patente = normalizar(req.patente());
        if (repository.existsByPatenteIgnoreCaseAndIdNot(patente, id)) {
            throw new ReglaNegocioException("Ya existe otro vehículo con la patente " + patente);
        }
        // Solo se valida la sucursal si el vehículo cambia de sucursal.
        if (!vehiculo.getSucursal().getId().equals(req.sucursalId())) {
            vehiculo.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
        }
        aplicar(vehiculo, req, patente);
        return VehiculoResponse.from(vehiculo);
    }

    @Transactional
    public VehiculoResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Vehiculo vehiculo = buscar(id);
        vehiculo.setDisponible(disponible);
        return VehiculoResponse.from(vehiculo);
    }

    private Vehiculo buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", id));
    }

   

    private void aplicar(Vehiculo v, VehiculoRequest req, String patente) {
        v.setPatente(patente);
        v.setModelo(req.modelo().trim());
        v.setCapacidadPesoKg(req.capacidadPesoKg());
        v.setCapacidadVolumenM3(req.capacidadVolumenM3());
    }

    private String normalizar(String patente) {
        return patente.trim().toUpperCase();
    }
}