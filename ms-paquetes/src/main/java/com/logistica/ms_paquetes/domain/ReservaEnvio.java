package com.logistica.ms_paquetes.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reserva_envio",
       uniqueConstraints = @UniqueConstraint(name = "uk_reserva_envio_envio", columnNames = "envio_id"))
@Getter
@Setter
@NoArgsConstructor
public class ReservaEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID externo de ms-logistica; no se crea una FK entre microservicios. */
    @Column(name = "envio_id", nullable = false, unique = true)
    private Long envioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;

    @Column(name = "repartidor_sub", nullable = false, length = 100)
    private String repartidorSub;

    @Column(name = "peso_total_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoTotalKg;

    @Column(name = "volumen_total_m3", nullable = false, precision = 14, scale = 6)
    private BigDecimal volumenTotalM3;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "fecha_reserva", nullable = false, updatable = false)
    private Instant fechaReserva = Instant.now();

    @Column(name = "fecha_liberacion")
    private Instant fechaLiberacion;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaEnvioPaquete> paquetes = new ArrayList<>();

    public void agregarPaquete(Paquete paquete) {
        paquetes.add(new ReservaEnvioPaquete(this, paquete));
    }

    public void liberar() {
        if (activa) {
            activa = false;
            fechaLiberacion = Instant.now();
        }
    }
}
