package com.logistica.ms_paquetes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.ms_paquetes.dto.PaqueteLogisticaResponse;
import com.logistica.ms_paquetes.dto.EstadoPaqueteRequest;
import com.logistica.ms_paquetes.dto.RepartidorResponse;
import com.logistica.ms_paquetes.dto.ReservaEnvioRequest;
import com.logistica.ms_paquetes.dto.ReservaEnvioResponse;
import com.logistica.ms_paquetes.dto.VehiculoResponse;
import com.logistica.ms_paquetes.service.PaqueteService;
import com.logistica.ms_paquetes.service.ReservaEnvioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/interno/logistica")
@RequiredArgsConstructor
public class LogisticaInternaController {

    private final PaqueteService paqueteService;
    private final ReservaEnvioService reservaService;

    @GetMapping("/paquetes")
    public List<PaqueteLogisticaResponse> paquetesPorIds(@RequestParam List<Long> ids) {
        return paqueteService.obtenerParaLogistica(ids);
    }

    @PatchMapping("/paquetes/{id}/estado")
    public PaqueteLogisticaResponse cambiarEstadoPaquete(
            @PathVariable Long id,
            @Valid @RequestBody EstadoPaqueteRequest req
    ) {
        return paqueteService.cambiarEstadoParaLogistica(id, req.estado());
    }

    @GetMapping("/vehiculos/disponibles")
    public List<VehiculoResponse> vehiculosDisponibles(@RequestParam Long sucursalId) {
        return reservaService.vehiculosDisponibles(sucursalId);
    }

    @GetMapping("/repartidores/disponibles")
    public List<RepartidorResponse> repartidoresDisponibles(@RequestParam Long sucursalId) {
        return reservaService.repartidoresDisponibles(sucursalId);
    }

    @PutMapping("/envios/{envioId}/reserva")
    public ReservaEnvioResponse reservar(
            @PathVariable Long envioId,
            @Valid @RequestBody ReservaEnvioRequest req
    ) {
        return reservaService.reservar(envioId, req);
    }

    @DeleteMapping("/envios/{envioId}/reserva")
    public ResponseEntity<Void> liberar(@PathVariable Long envioId) {
        reservaService.liberar(envioId);
        return ResponseEntity.noContent().build();
    }
}
