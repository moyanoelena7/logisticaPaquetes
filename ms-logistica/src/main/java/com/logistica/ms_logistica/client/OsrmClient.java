package com.logistica.ms_logistica.client;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.logistica.ms_logistica.exception.CalculoRutaNoPosibleException;
import com.logistica.ms_logistica.exception.RespuestaOsrmInvalidaException;

import org.springframework.http.HttpStatusCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OsrmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OsrmClient(@Qualifier("osrmRestClient") RestClient osrmRestClient, ObjectMapper objectMapper) {
        this.restClient = osrmRestClient;
        this.objectMapper = objectMapper;
    }

    public TripResponse calcularViaje(String coordenadas) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trip/v1/driving/" + coordenadas)
                        .queryParam("roundtrip", "false")
                        .queryParam("source", "first")
                        .queryParam("destination", "any")
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .queryParam("steps", "false")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    try {
                        JsonNode body = objectMapper.readTree(response.getBody());
                        if (body == null || !body.isObject()) {
                            throw new RespuestaOsrmInvalidaException(
                                    "OSRM devolvió un error sin un objeto JSON válido");
                        }
                        String code = body.path("code").asText("");
                        if ("NoSegment".equals(code) || "NoTrips".equals(code)) {
                            throw new CalculoRutaNoPosibleException(
                                    "OSRM no encontró un recorrido válido para todos los destinos (" + code + ")");
                        }
                        throw new RespuestaOsrmInvalidaException(
                                "OSRM rechazó la solicitud de ruta (" + code + ")");
                    } catch (JacksonException exception) {
                        throw new RespuestaOsrmInvalidaException(
                                "OSRM devolvió un error sin un cuerpo JSON válido");
                    }
                })
                .body(TripResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TripResponse(
            String code,
            List<Trip> trips,
            List<Waypoint> waypoints
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Trip(
            BigDecimal distance,
            BigDecimal duration,
            JsonNode geometry
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Waypoint(
            @JsonProperty("trips_index") Integer tripsIndex,
            @JsonProperty("waypoint_index") Integer waypointIndex
    ) {
    }
}
