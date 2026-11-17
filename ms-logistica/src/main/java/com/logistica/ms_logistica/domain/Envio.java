package com.logistica.ms_logistica.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "envio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    @Column(nullable = false, length = 20)
    private String sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private EstadoEnvio estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_inicio")
    @Setter
    private Instant fechaInicio;

    @Column(name = "fecha_fin")
    @Setter
    private Instant fechaFin;

    @OneToMany(mappedBy = "envio", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EnvioPaquete> paquetes = new ArrayList<>();

    @OneToOne(mappedBy = "envio", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Setter
    private Asignacion asignacion;

    @OneToOne(mappedBy = "envio", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Setter
    private RutaPlanificada ruta;

    public Envio(Long sucursalId, String sector, EstadoEnvio estado, Instant fechaCreacion) {
        this.sucursalId = sucursalId;
        this.sector = sector;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }
}
