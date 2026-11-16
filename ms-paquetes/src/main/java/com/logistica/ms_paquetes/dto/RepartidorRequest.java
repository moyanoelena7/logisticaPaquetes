package com.logistica.ms_paquetes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RepartidorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        @Pattern(regexp = "^[0-9+()\\-\\s]*$",
                 message = "El teléfono solo admite números, espacios, +, - y paréntesis")
        String telefono,

        @NotBlank(message = "El usuario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$",
                 message = "El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "El subject de Keycloak es obligatorio")
        @Size(max = 100, message = "El subject de Keycloak no puede superar 100 caracteres")
        String keycloakSub,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId
) { }
