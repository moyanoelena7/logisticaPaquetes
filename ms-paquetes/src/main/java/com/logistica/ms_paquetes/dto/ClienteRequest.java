package com.logistica.ms_paquetes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        @Pattern(regexp = "^[0-9+()\\-\\s]*$",
                 message = "El teléfono solo admite números, espacios, +, - y paréntesis")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar 150 caracteres")
        String email
) { }