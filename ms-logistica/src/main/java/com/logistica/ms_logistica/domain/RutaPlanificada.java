package com.logistica.ms_logistica.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ruta_planificada")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RutaPlanificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false, unique = true)
    private Envio envio;

    @Column(name = "distancia_metros", nullable = false, precision = 14, scale = 3)
    private BigDecimal distanciaMetros;

    @Column(name = "duracion_segundos", nullable = false, precision = 14, scale = 3)
    private BigDecimal duracionSegundos;

    @Lob
    @Column(name = "geometria_geojson", nullable = false)
    private String geometriaGeojson;

    @Column(name = "fecha_calculo", nullable = false)
    private Instant fechaCalculo;

    @OneToMany(mappedBy = "ruta", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("ordenVisita ASC")
    private List<ParadaRuta> paradas = new ArrayList<>();

    public RutaPlanificada(Envio envio, BigDecimal distanciaMetros, BigDecimal duracionSegundos,
                           String geometriaGeojson, Instant fechaCalculo) {
        this.envio = envio;
        this.distanciaMetros = distanciaMetros;
        this.duracionSegundos = duracionSegundos;
        this.geometriaGeojson = geometriaGeojson;
        this.fechaCalculo = fechaCalculo;
    }
}
