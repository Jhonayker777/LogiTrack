package LogiTrack.API.dto.response;

import java.time.LocalDate;

public record UsuarioDtoResponse(
        Long id,
        String nombre,
        String apellido,
        String documento,
        String email,
        String telefono,
        String direccion,
        CiudadDtoResponse ciudad,
        String username,
        String rol,
        String turno,
        LocalDate fechaIngreso,
        Boolean activo
) {
}