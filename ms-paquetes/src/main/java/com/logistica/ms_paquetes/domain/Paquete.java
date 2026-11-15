package com.logistica.ms_paquetes.domain;

import java.math.BigDecimal;
import java.time.Instant;

import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paquete")
@Getter
@Setter
@NoArgsConstructor
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sector sector = Sector.CENTRAL;

    @Column(name = "direccion_envio", nullable = false, length = 255)
    private String direccionEnvio;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "alto_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal altoCm;

    @Column(name = "ancho_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal anchoCm;

    @Column(name = "largo_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal largoCm;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPaquete estado = EstadoPaquete.PENDIENTE;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro = Instant.now();
}