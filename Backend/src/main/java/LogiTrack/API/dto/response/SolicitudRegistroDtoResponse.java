package LogiTrack.API.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudRegistroDtoResponse(
        Long id,
        String nombre,
        String apellido,
        String documento,
        String email,
        String telefono,
        String direccion,
        CiudadDtoResponse ciudad,
        String username,
        String rolSolicitado,
        String turno,
        LocalDate fechaIngreso,
        String estado,
        String motivoRechazo,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaResolucion,
        UsuarioResumenDtoResponse resueltoPor,
        UsuarioResumenDtoResponse usuarioCreado
) {
}