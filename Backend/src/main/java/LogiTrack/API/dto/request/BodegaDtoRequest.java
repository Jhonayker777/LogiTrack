package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;

public record BodegaDtoRequest(

        @NotBlank(message = "El nombre de la bodega es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "La ubicación es obligatoria")
        @Size(min = 2, max = 200, message = "La ubicación debe tener entre 2 y 200 caracteres")
        String ubicacion,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a 0")
        Integer capacidad,

        @Positive(message = "El id del encargado debe ser válido")
        Long encargadoId,

        @Positive(message = "El id de la ciudad debe ser válido")
        Long ciudadId

) {
}