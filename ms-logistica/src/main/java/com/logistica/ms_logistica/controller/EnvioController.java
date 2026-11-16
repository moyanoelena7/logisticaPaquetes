package com.logistica.ms_logistica.controller;

import java.net.URI;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.ms_logistica.domain.EstadoEnvio;
import com.logistica.ms_logistica.dto.CrearEnvioRequest;
import com.logistica.ms_logistica.dto.EnvioCreadoResponse;
import com.logistica.ms_logistica.dto.EnvioDetalleResponse;
import com.logistica.ms_logistica.dto.AsignacionRequest;
import com.logistica.ms_logistica.dto.RecursosDisponiblesResponse;
import com.logistica.ms_logistica.dto.RegistrarResultadoEntregaRequest;
import com.logistica.ms_logistica.dto.RutaResponse;
import com.logistica.ms_logistica.service.EnvioApplicationService;
import com.logistica.ms_logistica.service.EnvioConsultaService;
import com.logistica.ms_logistica.service.RepartoService;
import com.logistica.ms_logistica.service.RutaConsultaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioApplicationService envioApplicationService;
    private final EnvioConsultaService envioConsultaService;
    private final RepartoService repartoService;
    private final RutaConsultaService rutaConsultaService;

    @GetMapping
    public List<EnvioDetalleResponse> buscar(
            @RequestParam(required = false) EstadoEnvio estado,
            @RequestParam(required = false) String sector
    ) {
        return envioConsultaService.buscar(estado, sector);
    }

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

    @GetMapping("/{envioId}")
    public EnvioDetalleResponse obtener(
            @PathVariable("envioId") Long envioId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        return envioConsultaService.obtener(envioId, jwt.getSubject(), esAdministrador(authentication));
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

    @PatchMapping("/{envioId}/inicio")
    public ResponseEntity<Void> iniciar(
            @PathVariable("envioId") Long envioId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        repartoService.iniciar(envioId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{envioId}/paquetes/{paqueteId}/resultado")
    public ResponseEntity<Void> registrarResultado(
            @PathVariable("envioId") Long envioId,
            @PathVariable("paqueteId") Long paqueteId,
            @Valid @RequestBody RegistrarResultadoEntregaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        repartoService.registrarResultado(envioId, paqueteId, jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{envioId}/cierre")
    public ResponseEntity<Void> cerrar(
            @PathVariable("envioId") Long envioId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        repartoService.cerrar(envioId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRADOR"::equals);
    }
}
