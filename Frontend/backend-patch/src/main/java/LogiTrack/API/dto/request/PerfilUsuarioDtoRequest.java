package LogiTrack.API.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Campos que un usuario puede modificar de su propio perfil.
 * No contiene username, rol, estado ni permisos.
 */
public record PerfilUsuarioDtoRequest(
        @NotBlank @Size(min = 2, max = 100) String nombre,
        @NotBlank @Size(min = 2, max = 100) String apellido,
        @Size(max = 30) String documento,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        @Size(max = 200) String direccion,
        @Positive Long ciudadId
) {}
