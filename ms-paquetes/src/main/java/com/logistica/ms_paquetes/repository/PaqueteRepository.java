package com.logistica.ms_paquetes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistica.ms_paquetes.domain.Paquete;
import com.logistica.ms_paquetes.domain.Sector;
import com.logistica.ms_paquetes.domain.estado.EstadoPaquete;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    List<Paquete> findByEstado(EstadoPaquete estado);

    List<Paquete> findBySector(Sector sector);

    List<Paquete> findByEstadoAndSector(EstadoPaquete estado, Sector sector);
}