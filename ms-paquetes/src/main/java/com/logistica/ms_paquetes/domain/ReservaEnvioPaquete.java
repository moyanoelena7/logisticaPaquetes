package com.logistica.ms_paquetes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reserva_envio_paquete",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_reserva_envio_paquete",
               columnNames = {"reserva_id", "paquete_id"}))
@Getter
@NoArgsConstructor
public class ReservaEnvioPaquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaEnvio reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paquete_id", nullable = false)
    private Paquete paquete;

    public ReservaEnvioPaquete(ReservaEnvio reserva, Paquete paquete) {
        this.reserva = reserva;
        this.paquete = paquete;
    }
}
