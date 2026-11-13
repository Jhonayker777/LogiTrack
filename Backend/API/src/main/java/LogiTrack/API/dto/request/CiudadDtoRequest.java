package LogiTrack.API.dto.request;

import jakarta.validation.constraints.*;

public record CiudadDtoRequest(

        @NotBlank(message = "El nombre de la ciudad es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 20, message = "El código postal no puede superar 20 caracteres")
        String codigoPostal,

        @NotNull(message = "El departamento es obligatorio")
        @Positive(message = "El id del departamento debe ser válido")
        Long departamentoId

) {
}