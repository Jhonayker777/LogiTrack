package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoDtoRequest(

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(min = 2, max = 100, message = "La categoría debe tener entre 2 y 100 caracteres")
        String categoria,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock mínimo es obligatorio")
        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo

) {
}