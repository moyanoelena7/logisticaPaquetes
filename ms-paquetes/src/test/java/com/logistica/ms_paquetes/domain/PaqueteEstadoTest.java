package com.logistica.ms_paquetes.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.logistica.ms_paquetes.exception.ReglaNegocioException;

class PaqueteEstadoTest {
    static Stream<Arguments> transiciones() {
        List<String> estados = List.of("PENDIENTE", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "INCIDENCIA");
        return estados.stream().flatMap(origen -> Stream.of(
                Arguments.of(origen, "ASIGNADO", (Consumer<Paquete>) Paquete::asignar,
                        origen.equals("PENDIENTE") || origen.equals("ASIGNADO")),
                Arguments.of(origen, "EN_CAMINO", (Consumer<Paquete>) Paquete::enCamino,
                        origen.equals("ASIGNADO") || origen.equals("EN_CAMINO")),
                Arguments.of(origen, "ENTREGADO", (Consumer<Paquete>) Paquete::entregar,
                        origen.equals("EN_CAMINO") || origen.equals("ENTREGADO")),
                Arguments.of(origen, "INCIDENCIA", (Consumer<Paquete>) Paquete::registrarIncidencia,
                        origen.equals("EN_CAMINO") || origen.equals("INCIDENCIA"))));
    }

    @ParameterizedTest
    @MethodSource("transiciones")
    void elEstadoActualResuelveLaOperacion(String origen, String destino,
            Consumer<Paquete> operacion, boolean permitida) {
        Paquete paquete = enEstado(origen);
        if (permitida) {
            operacion.accept(paquete);
            assertEquals(destino, paquete.getEstado().nombre());
        } else {
            assertThrows(ReglaNegocioException.class, () -> operacion.accept(paquete));
            assertEquals(origen, paquete.getEstado().nombre());
        }
    }

    @Test
    void soloPendientePermiteEditarYReservar() {
        for (String estado : List.of("PENDIENTE", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "INCIDENCIA")) {
            Paquete paquete = enEstado(estado);
            if (estado.equals("PENDIENTE")) {
                assertDoesNotThrow(paquete::validarEdicion);
                assertDoesNotThrow(paquete::validarReserva);
            } else {
                assertThrows(ReglaNegocioException.class, paquete::validarEdicion);
                assertThrows(ReglaNegocioException.class, paquete::validarReserva);
            }
        }
    }

    @Test
    void unaTransicionNoModificaOtrosPaquetes() {
        Paquete primero = new Paquete();
        Paquete segundo = new Paquete();
        primero.asignar();
        assertEquals("PENDIENTE", segundo.getEstado().nombre());
    }

    public static Paquete enEstado(String estado) {
        Paquete paquete = new Paquete();
        if (!estado.equals("PENDIENTE")) paquete.asignar();
        if (List.of("EN_CAMINO", "ENTREGADO", "INCIDENCIA").contains(estado)) paquete.enCamino();
        if (estado.equals("ENTREGADO")) paquete.entregar();
        if (estado.equals("INCIDENCIA")) paquete.registrarIncidencia();
        return paquete;
    }
}
