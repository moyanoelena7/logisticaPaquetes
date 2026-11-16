package com.logistica.ms_logistica.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "envio_paquete", uniqueConstraints = @UniqueConstraint(
        name = "uk_envio_paquete", columnNames = {"envio_id", "paquete_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnvioPaquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Column(name = "paquete_id", nullable = false)
    private Long paqueteId;

    @Column(name = "direccion_destino")
    @Setter
    private String direccionDestino;

    @Column(name = "latitud_destino", precision = 10, scale = 7)
    @Setter
    private BigDecimal latitudDestino;

    @Column(name = "longitud_destino", precision = 10, scale = 7)
    @Setter
    private BigDecimal longitudDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private ResultadoEntrega resultado;

    @Column(name = "fecha_intento")
    @Setter
    private Instant fechaIntento;

    @Lob
    @Column(name = "motivo_incidencia")
    @Setter
    private String motivoIncidencia;

    public EnvioPaquete(Envio envio, Long paqueteId, ResultadoEntrega resultado) {
        this.envio = envio;
        this.paqueteId = paqueteId;
        this.resultado = resultado;
    }
}
