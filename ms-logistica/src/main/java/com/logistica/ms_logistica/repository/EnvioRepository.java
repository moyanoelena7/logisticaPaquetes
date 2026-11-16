package com.logistica.ms_logistica.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.logistica.ms_logistica.domain.Envio;

import jakarta.persistence.LockModeType;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Envio e where e.id = :id")
    Optional<Envio> buscarParaActualizar(@Param("id") Long id);
}
