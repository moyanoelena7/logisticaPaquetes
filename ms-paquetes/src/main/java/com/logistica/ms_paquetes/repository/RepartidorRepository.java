package com.logistica.ms_paquetes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistica.ms_paquetes.domain.Repartidor;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    List<Repartidor> findByDisponible(boolean disponible);
}