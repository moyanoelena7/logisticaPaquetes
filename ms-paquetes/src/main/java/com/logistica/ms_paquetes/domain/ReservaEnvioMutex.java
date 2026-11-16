package com.logistica.ms_paquetes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de sincronización que serializa operaciones concurrentes del mismo envioId. */
@Entity
@Table(name = "reserva_envio_mutex")
@Getter
@NoArgsConstructor
public class ReservaEnvioMutex {

    @Id
    @Column(name = "envio_id", nullable = false)
    private Long envioId;
}
