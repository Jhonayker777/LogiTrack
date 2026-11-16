package LogiTrack.API.dto.request;

import LogiTrack.API.Model.Usuario;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record SolicitudRegistroDtoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String apellido,

        @Size(max = 30, message = "El documento no puede superar 30 caracteres")
        String documento,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar 150 caracteres")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Positive(message = "El id de la ciudad debe ser válido")
        Long ciudadId,

        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @NotNull(message = "El rol solicitado es obligatorio")
        Usuario.Rol rolSolicitado,

        @Size(max = 50, message = "El turno no puede superar 50 caracteres")
        String turno,

        LocalDate fechaIngreso

) {
}