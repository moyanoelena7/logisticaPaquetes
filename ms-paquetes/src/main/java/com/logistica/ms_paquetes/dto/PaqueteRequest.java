package com.logistica.ms_paquetes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaqueteRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotNull(message = "La sucursal es obligatoria")
        Long sucursalId,

        @NotBlank(message = "La dirección de envío es obligatoria")
        @Size(max = 255, message = "La dirección de envío no puede superar 255 caracteres")
        String direccionEnvio,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        @Digits(integer = 2, fraction = 7, message = "La latitud admite hasta 7 decimales")
        BigDecimal latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        @Digits(integer = 3, fraction = 7, message = "La longitud admite hasta 7 decimales")
        BigDecimal longitud,

        @NotNull(message = "El alto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El alto debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El alto admite hasta 8 enteros y 2 decimales")
        BigDecimal altoCm,

        @NotNull(message = "El ancho es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El ancho debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El ancho admite hasta 8 enteros y 2 decimales")
        BigDecimal anchoCm,

        @NotNull(message = "El largo es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El largo debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El largo admite hasta 8 enteros y 2 decimales")
        BigDecimal largoCm,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El peso debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El peso admite hasta 8 enteros y 2 decimales")
        BigDecimal pesoKg
) { }