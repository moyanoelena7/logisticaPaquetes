package com.logistica.ms_paquetes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistica.ms_paquetes.domain.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatenteIgnoreCase(String patente);

    boolean existsByPatenteIgnoreCaseAndIdNot(String patente, Long id);

    List<Vehiculo> findByDisponible(boolean disponible);
}