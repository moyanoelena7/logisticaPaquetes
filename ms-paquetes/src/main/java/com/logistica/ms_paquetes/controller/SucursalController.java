package com.logistica.ms_paquetes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.ms_paquetes.dto.SucursalRequest;
import com.logistica.ms_paquetes.dto.SucursalResponse;
import com.logistica.ms_paquetes.service.SucursalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService service;

    @PostMapping
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest req) {
        SucursalResponse creada = service.crear(req);
        return ResponseEntity.created(URI.create("/sucursales/" + creada.id())).body(creada);
    }

    @GetMapping
    public List<SucursalResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SucursalResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public SucursalResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody SucursalRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/desactivacion")
    public SucursalResponse desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }
}