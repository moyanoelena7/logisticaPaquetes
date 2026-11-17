package com.logistica.ms_paquetes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.logistica.ms_paquetes.domain.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatenteIgnoreCase(String patente);

    boolean existsByPatenteIgnoreCaseAndIdNot(String patente, Long id);

    List<Vehiculo> findByDisponible(boolean disponible);

    List<Vehiculo> findBySucursal_IdAndDisponibleTrue(Long sucursalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehiculo v where v.id = :id")
    Optional<Vehiculo> bloquearPorId(@Param("id") Long id);
}
