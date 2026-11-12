package com.logistica.ms_paquetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistica.ms_paquetes.domain.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
}