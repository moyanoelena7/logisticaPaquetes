package com.logistica.ms_logistica.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asignacion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false, unique = true)
    private Envio envio;

    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @Column(name = "repartidor_id", nullable = false)
    private Long repartidorId;

    @Column(name = "repartidor_sub", nullable = false)
    private String repartidorSub;

    @Column(name = "peso_total_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoTotalKg;

    @Column(name = "volumen_total_m3", nullable = false, precision = 19, scale = 12)
    private BigDecimal volumenTotalM3;

    @Column(name = "latitud_salida", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitudSalida;

    @Column(name = "longitud_salida", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitudSalida;

    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion;

    public Asignacion(Envio envio, Long vehiculoId, Long repartidorId, String repartidorSub,
                      BigDecimal pesoTotalKg, BigDecimal volumenTotalM3,
                      BigDecimal latitudSalida, BigDecimal longitudSalida, Instant fechaAsignacion) {
        this.envio = envio;
        this.vehiculoId = vehiculoId;
        this.repartidorId = repartidorId;
        this.repartidorSub = repartidorSub;
        this.pesoTotalKg = pesoTotalKg;
        this.volumenTotalM3 = volumenTotalM3;
        this.latitudSalida = latitudSalida;
        this.longitudSalida = longitudSalida;
        this.fechaAsignacion = fechaAsignacion;
    }
}
