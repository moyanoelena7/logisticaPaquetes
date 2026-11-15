package com.logistica.ms_paquetes.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculo",
       uniqueConstraints = @UniqueConstraint(name = "uk_vehiculo_patente", columnNames = "patente"))
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false, length = 10)
    private String patente;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(name = "capacidad_peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPesoKg;

    @Column(name = "capacidad_volumen_m3", nullable = false, precision = 10, scale = 3)
    private BigDecimal capacidadVolumenM3;

    @Column(nullable = false)
    private boolean disponible = true;
}