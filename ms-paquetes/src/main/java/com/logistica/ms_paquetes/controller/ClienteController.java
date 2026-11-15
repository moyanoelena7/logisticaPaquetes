package com.logistica.ms_paquetes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.ms_paquetes.dto.ClienteRequest;
import com.logistica.ms_paquetes.dto.ClienteResponse;
import com.logistica.ms_paquetes.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest req) {
        ClienteResponse creado = service.crear(req);
        return ResponseEntity.created(URI.create("/clientes/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String nombre) {
        return service.listar(nombre);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ClienteRequest req) {
        return service.actualizar(id, req);
    }
}