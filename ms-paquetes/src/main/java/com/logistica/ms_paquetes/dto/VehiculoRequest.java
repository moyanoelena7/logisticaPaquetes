package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehiculoRequest(
        @NotBlank(message = "La patente es obligatoria")
        @Pattern(regexp = "^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$",
                 flags = Pattern.Flag.CASE_INSENSITIVE,
                 message = "La patente debe tener formato ABC123 o AB123CD")
        String patente,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
        String modelo,

        @NotNull(message = "La capacidad de peso es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La capacidad de peso debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "La capacidad de peso admite hasta 8 enteros y 2 decimales")
        BigDecimal capacidadPesoKg,

        @NotNull(message = "La capacidad de volumen es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La capacidad de volumen debe ser mayor a cero")
        @Digits(integer = 7, fraction = 3, message = "La capacidad de volumen admite hasta 7 enteros y 3 decimales")
        BigDecimal capacidadVolumenM3,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId
) { }