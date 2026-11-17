package com.logistica.ms_logistica.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parada_ruta", uniqueConstraints = @UniqueConstraint(
        name = "uk_parada_ruta_orden", columnNames = {"ruta_id", "orden_visita"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParadaRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private RutaPlanificada ruta;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_paquete_id", nullable = false, unique = true)
    private EnvioPaquete envioPaquete;

    @Column(name = "orden_visita", nullable = false)
    private int ordenVisita;

    public ParadaRuta(RutaPlanificada ruta, EnvioPaquete envioPaquete, int ordenVisita) {
        this.ruta = ruta;
        this.envioPaquete = envioPaquete;
        this.ordenVisita = ordenVisita;
    }
}
