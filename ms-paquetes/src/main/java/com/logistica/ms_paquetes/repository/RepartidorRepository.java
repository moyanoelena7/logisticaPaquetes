package com.logistica.ms_paquetes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.logistica.ms_paquetes.domain.Repartidor;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByKeycloakSub(String keycloakSub);

    boolean existsByKeycloakSubAndIdNot(String keycloakSub, Long id);

    List<Repartidor> findByDisponible(boolean disponible);

    List<Repartidor> findBySucursal_IdAndDisponibleTrue(Long sucursalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Repartidor r where r.id = :id")
    Optional<Repartidor> bloquearPorId(@Param("id") Long id);
}
