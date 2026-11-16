package com.logistica.ms_paquetes.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    List<Paquete> findByEstado(EstadoPaquete estado);

    List<Paquete> findBySector(Sector sector);

    List<Paquete> findByEstadoAndSector(EstadoPaquete estado, Sector sector);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paquete p where p.id in :ids order by p.id")
    List<Paquete> bloquearPorIds(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paquete p where p.id = :id")
    java.util.Optional<Paquete> bloquearPorId(@Param("id") Long id);
}
