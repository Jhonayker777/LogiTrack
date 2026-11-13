package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;

public record DepartamentoDtoRequest(

        @NotBlank(message = "El nombre del departamento es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El país no puede superar 100 caracteres")
        String pais

) {
}