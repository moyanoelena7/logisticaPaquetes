package com.logistica.ms_logistica.controller;

import java.net.URI;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.dto.CrearEnvioRequest;
import com.logistica.ms_logistica.dto.EnvioCreadoResponse;
import com.logistica.ms_logistica.dto.AsignacionRequest;
import com.logistica.ms_logistica.dto.RecursosDisponiblesResponse;
import com.logistica.ms_logistica.dto.RutaResponse;
import com.logistica.ms_logistica.service.EnvioApplicationService;
import com.logistica.ms_logistica.service.RutaConsultaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioApplicationService envioApplicationService;
    private final RutaConsultaService rutaConsultaService;

    @GetMapping("/mias/rutas")
    public List<RutaResponse> obtenerMisRutas(@AuthenticationPrincipal Jwt jwt) {
        return rutaConsultaService.obtenerMisRutas(jwt.getSubject());
    }

    @GetMapping("/{envioId}/ruta")
    public RutaResponse obtenerRuta(
            @PathVariable("envioId") Long envioId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        boolean administrador = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRADOR"::equals);
        return rutaConsultaService.obtenerRuta(envioId, jwt.getSubject(), administrador);
    }

    @GetMapping("/disponibilidad")
    public RecursosDisponiblesResponse obtenerDisponibilidad(
            @RequestParam("sucursalId") @Positive Long sucursalId
    ) {
        return envioApplicationService.obtenerRecursosDisponibles(sucursalId);
    }

    @PostMapping
    public ResponseEntity<EnvioCreadoResponse> crear(@Valid @RequestBody CrearEnvioRequest request) {
        Long envioId = envioApplicationService.crearBorrador(request);
        EnvioCreadoResponse response = new EnvioCreadoResponse(envioId, EstadoEnvio.CREADO);

        return ResponseEntity
                .created(URI.create("/envios/" + envioId))
                .body(response);
    }

    @PostMapping("/{envioId}/asignaciones")
    public ResponseEntity<Void> asignar(
            @PathVariable("envioId") Long envioId,
            @Valid @RequestBody AsignacionRequest request
    ) {
        envioApplicationService.asignar(envioId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{envioId}/ruta")
    public RutaResponse planificarRuta(@PathVariable("envioId") Long envioId) {
        return envioApplicationService.planificarRuta(envioId);
    }
}
