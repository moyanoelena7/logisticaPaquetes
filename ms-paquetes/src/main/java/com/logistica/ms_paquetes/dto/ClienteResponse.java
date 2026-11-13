package com.logistica.ms_paquetes.dto;

import com.logistica.ms_paquetes.domain.Cliente;

public record ClienteResponse(Long id, String nombre, String telefono, String email) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail());
    }
}