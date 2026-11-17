package com.logistica.ms_paquetes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistica.ms_paquetes.domain.Repartidor;
import com.logistica.ms_paquetes.dto.RepartidorRequest;
import com.logistica.ms_paquetes.dto.RepartidorResponse;
import com.logistica.ms_paquetes.exception.RecursoNoEncontradoException;
import com.logistica.ms_paquetes.exception.ReglaNegocioException;
import com.logistica.ms_paquetes.repository.RepartidorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepartidorService {

    private final RepartidorRepository repository;
    private final SucursalService sucursalService;

    @Transactional
    public RepartidorResponse crear(RepartidorRequest req) {
        String username = normalizar(req.username());
        String keycloakSub = req.keycloakSub().trim();
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new ReglaNegocioException("Ya existe un repartidor con el usuario " + username);
        }
        if (repository.existsByKeycloakSub(keycloakSub)) {
            throw new ReglaNegocioException("Ya existe un repartidor vinculado al subject de Keycloak " + keycloakSub);
        }
        Repartidor repartidor = new Repartidor();
        repartidor.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
        aplicar(repartidor, req, username, keycloakSub);
        return RepartidorResponse.from(repository.save(repartidor));
    }

    @Transactional(readOnly = true)
    public List<RepartidorResponse> listar(Boolean disponible) {
        List<Repartidor> repartidores = (disponible == null)
                ? repository.findAll()
                : repository.findByDisponible(disponible);
        return repartidores.stream().map(RepartidorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RepartidorResponse obtener(Long id) {
        return RepartidorResponse.from(buscar(id));
    }

    @Transactional
    public RepartidorResponse actualizar(Long id, RepartidorRequest req) {
        Repartidor repartidor = buscar(id);
        String username = normalizar(req.username());
        String keycloakSub = req.keycloakSub().trim();
        if (repository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ReglaNegocioException("Ya existe otro repartidor con el usuario " + username);
        }
        if (repository.existsByKeycloakSubAndIdNot(keycloakSub, id)) {
            throw new ReglaNegocioException("El subject de Keycloak ya está vinculado a otro repartidor");
        }
        // Solo se valida la sucursal si el repartidor cambia de sucursal.
        if (!repartidor.getSucursal().getId().equals(req.sucursalId())) {
            repartidor.setSucursal(sucursalService.obtenerAsignable(req.sucursalId()));
        }
        aplicar(repartidor, req, username, keycloakSub);
        return RepartidorResponse.from(repartidor);
    }

    @Transactional
    public RepartidorResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Repartidor repartidor = buscar(id);
        repartidor.setDisponible(disponible);
        return RepartidorResponse.from(repartidor);
    }

    private Repartidor buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor", id));
    }

    private void aplicar(Repartidor r, RepartidorRequest req, String username, String keycloakSub) {
        r.setNombre(req.nombre().trim());
        r.setTelefono(vacioANull(req.telefono()));
        r.setUsername(username);
        r.setKeycloakSub(keycloakSub);
    }

    private String normalizar(String username) {
        return username.trim().toLowerCase();
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
