package com.logistica.ms_paquetes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;
import com.logistica.ms_paquetes.dto.EstadoPaqueteRequest;
import com.logistica.ms_paquetes.dto.PaqueteRequest;
import com.logistica.ms_paquetes.dto.PaqueteResponse;
import com.logistica.ms_paquetes.service.PaqueteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService service;

    @PostMapping
    public ResponseEntity<PaqueteResponse> crear(@Valid @RequestBody PaqueteRequest req) {
        PaqueteResponse creado = service.crear(req);
        return ResponseEntity.created(URI.create("/paquetes/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<PaqueteResponse> listar(
            @RequestParam(required = false) EstadoPaquete estado,
            @RequestParam(required = false) Sector sector) {
        return service.listar(estado, sector);
    }

    @GetMapping("/{id}")
    public PaqueteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public PaqueteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody PaqueteRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/estado")
    public PaqueteResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody EstadoPaqueteRequest req) {
        return service.cambiarEstado(id, req.estado());
    }
}