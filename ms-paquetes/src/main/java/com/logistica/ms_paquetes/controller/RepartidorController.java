package com.logistica.ms_paquetes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.logistica.ms_paquetes.dto.DisponibilidadRequest;
import com.logistica.ms_paquetes.dto.RepartidorRequest;
import com.logistica.ms_paquetes.dto.RepartidorResponse;
import com.logistica.ms_paquetes.service.RepartidorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final RepartidorService service;

    @PostMapping
    public ResponseEntity<RepartidorResponse> crear(@Valid @RequestBody RepartidorRequest req) {
        RepartidorResponse creado = service.crear(req);
        return ResponseEntity.created(URI.create("/repartidores/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<RepartidorResponse> listar(@RequestParam(required = false) Boolean disponible) {
        return service.listar(disponible);
    }

    @GetMapping("/{id}")
    public RepartidorResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public RepartidorResponse actualizar(@PathVariable Long id,
                                         @Valid @RequestBody RepartidorRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/disponibilidad")
    public RepartidorResponse cambiarDisponibilidad(@PathVariable Long id,
                                                    @Valid @RequestBody DisponibilidadRequest req) {
        return service.cambiarDisponibilidad(id, req.disponible());
    }
}