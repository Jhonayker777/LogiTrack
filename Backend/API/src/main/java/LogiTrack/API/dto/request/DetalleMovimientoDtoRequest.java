package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;

public record DetalleMovimientoDtoRequest(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El id del producto debe ser válido")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad

) {
}