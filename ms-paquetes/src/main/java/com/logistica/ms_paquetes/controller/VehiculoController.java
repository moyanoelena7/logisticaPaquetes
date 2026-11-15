package com.logistica.ms_paquetes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.logistica.ms_paquetes.dto.DisponibilidadRequest;
import com.logistica.ms_paquetes.dto.VehiculoRequest;
import com.logistica.ms_paquetes.dto.VehiculoResponse;
import com.logistica.ms_paquetes.service.VehiculoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService service;

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest req) {
        VehiculoResponse creado = service.crear(req);
        return ResponseEntity.created(URI.create("/vehiculos/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<VehiculoResponse> listar(@RequestParam(required = false) Boolean disponible) {
        return service.listar(disponible);
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody VehiculoRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/disponibilidad")
    public VehiculoResponse cambiarDisponibilidad(@PathVariable Long id,
                                                  @Valid @RequestBody DisponibilidadRequest req) {
        return service.cambiarDisponibilidad(id, req.disponible());
    }
}