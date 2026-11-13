package LogiTrack.API.dto.response;

public record UsuarioResumenDtoResponse(
        Long id,
        String nombreCompleto,
        String username,
        String rol
) {
}