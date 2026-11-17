package com.logistica.ms_logistica.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaquetesClient {

    private final RestClient restClient;

    public PaquetesClient(@Qualifier("paquetesRestClient") RestClient paquetesRestClient) {
        this.restClient = paquetesRestClient;
    }

    public List<PaqueteResumen> obtenerPaquetes(List<Long> ids) {
        String idsCsv = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/interno/logistica/paquetes")
                        .queryParam("ids", idsCsv)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<VehiculoDisponible> obtenerVehiculosDisponibles(Long sucursalId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/interno/logistica/vehiculos/disponibles")
                        .queryParam("sucursalId", sucursalId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<RepartidorDisponible> obtenerRepartidoresDisponibles(Long sucursalId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/interno/logistica/repartidores/disponibles")
                        .queryParam("sucursalId", sucursalId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ReservaResponse reservar(Long envioId, ReservaRequest request) {
        return restClient.put()
                .uri("/interno/logistica/envios/{envioId}/reserva", envioId)
                .body(request)
                .retrieve()
                .body(ReservaResponse.class);
    }

    public void liberarReserva(Long envioId) {
        restClient.delete()
                .uri("/interno/logistica/envios/{envioId}/reserva", envioId)
                .retrieve()
                .toBodilessEntity();
    }

    public void cambiarEstadoPaquete(Long paqueteId, String estado) {
        restClient.patch()
                .uri("/interno/logistica/paquetes/{paqueteId}/estado", paqueteId)
                .body(new EstadoPaqueteRequest(estado))
                .retrieve()
                .toBodilessEntity();
    }

    public record PaqueteResumen(
            Long id,
            Long sucursalId,
            String sector,
            String estado
    ) {}

    public record VehiculoDisponible(
            Long id,
            String patente,
            String modelo,
            BigDecimal capacidadPesoKg,
            BigDecimal capacidadVolumenM3,
            Long sucursalId,
            boolean disponible
    ) {}

    public record RepartidorDisponible(
            Long id,
            String nombre,
            String telefono,
            String username,
            String keycloakSub,
            Long sucursalId,
            boolean disponible
    ) {}

    public record ReservaRequest(
            Long sucursalId,
            String sector,
            List<Long> paqueteIds,
            Long vehiculoId,
            Long repartidorId
    ) {}

    public record EstadoPaqueteRequest(String estado) {}

    public record ReservaResponse(
            Long envioId,
            boolean activa,
            List<Long> paqueteIds,
            Long vehiculoId,
            Long repartidorId,
            String repartidorSub,
            BigDecimal pesoTotalKg,
            BigDecimal volumenTotalM3,
            Origen origen,
            List<Destino> destinos,
            Instant fechaReserva,
            Instant fechaLiberacion
    ) {}

    public record Origen(
            Long sucursalId,
            String nombre,
            String direccion,
            BigDecimal latitud,
            BigDecimal longitud
    ) {}

    public record Destino(
            Long paqueteId,
            String direccionEnvio,
            BigDecimal latitud,
            BigDecimal longitud
    ) {}
}
