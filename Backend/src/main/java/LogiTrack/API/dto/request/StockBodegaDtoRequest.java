package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;

public record StockBodegaDtoRequest(

        @NotNull(message = "La bodega es obligatoria")
        @Positive(message = "El id de la bodega debe ser válido")
        Long bodegaId,

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El id del producto debe ser válido")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Integer cantidad

) {
}