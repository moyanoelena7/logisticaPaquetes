package com.logistica.ms_paquetes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistica.ms_paquetes.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}