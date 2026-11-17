package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.logistica.ms_paquetes.domain.ReservaEnvio;

public record ReservaEnvioResponse(
        Long envioId,
        boolean activa,
        List<Long> paqueteIds,
        Long vehiculoId,
        Long repartidorId,
        String repartidorSub,
        BigDecimal pesoTotalKg,
        BigDecimal volumenTotalM3,
        OrigenReservaResponse origen,
        List<DestinoReservaResponse> destinos,
        Instant fechaReserva,
        Instant fechaLiberacion
) {
    public static ReservaEnvioResponse from(ReservaEnvio reserva) {
        List<DestinoReservaResponse> destinos = reserva.getPaquetes().stream()
                .map(rp -> new DestinoReservaResponse(
                        rp.getPaquete().getId(),
                        rp.getPaquete().getDireccionEnvio(),
                        rp.getPaquete().getLatitud(),
                        rp.getPaquete().getLongitud()))
                .toList();

        return new ReservaEnvioResponse(
                reserva.getEnvioId(),
                reserva.isActiva(),
                destinos.stream().map(DestinoReservaResponse::paqueteId).toList(),
                reserva.getVehiculo().getId(),
                reserva.getRepartidor().getId(),
                reserva.getRepartidorSub(),
                reserva.getPesoTotalKg(),
                reserva.getVolumenTotalM3(),
                new OrigenReservaResponse(
                        reserva.getSucursal().getId(),
                        reserva.getSucursal().getNombre(),
                        reserva.getSucursal().getDireccion(),
                        reserva.getSucursal().getLatitud(),
                        reserva.getSucursal().getLongitud()),
                destinos,
                reserva.getFechaReserva(),
                reserva.getFechaLiberacion()
        );
    }
}
