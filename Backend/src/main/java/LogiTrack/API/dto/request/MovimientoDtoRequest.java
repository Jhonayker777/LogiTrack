package LogiTrack.API.dto.request;

import LogiTrack.API.Model.Movimiento;
import jakarta.validation.constraints.*;


public record MovimientoDtoRequest(

        @NotNull(message = "El tipo de movimiento es obligatorio")
        Movimiento.Tipo tipo,

        @Positive(message = "El id de la bodega origen debe ser válido")
        Long bodegaOrigenId,

        @Positive(message = "El id de la bodega destino debe ser válido")
        Long bodegaDestinoId,

        @Size(max = 255, message = "La observación no puede superar 255 caracteres")
        String observacion,

        @NotNull
        Long productoId,

        @NotNull @Min(1)
        Integer cantidad
) {
}