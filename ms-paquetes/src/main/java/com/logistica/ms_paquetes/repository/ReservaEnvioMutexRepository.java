package com.logistica.ms_paquetes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.logistica.ms_paquetes.domain.ReservaEnvioMutex;

import jakarta.persistence.LockModeType;

public interface ReservaEnvioMutexRepository extends JpaRepository<ReservaEnvioMutex, Long> {

    @Modifying
    @Query(value = "INSERT INTO reserva_envio_mutex (envio_id) VALUES (:envioId) "
            + "ON DUPLICATE KEY UPDATE envio_id = VALUES(envio_id)", nativeQuery = true)
    int asegurarFila(@Param("envioId") Long envioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ReservaEnvioMutex m where m.envioId = :envioId")
    Optional<ReservaEnvioMutex> bloquearPorEnvioId(@Param("envioId") Long envioId);
}
