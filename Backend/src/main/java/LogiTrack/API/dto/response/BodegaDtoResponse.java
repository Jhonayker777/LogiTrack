package LogiTrack.API.dto.response;

public record BodegaDtoResponse(
        Long id,
        String nombre,
        String ubicacion,
        Integer capacidad,
        UsuarioResumenDtoResponse encargado,
        CiudadDtoResponse ciudad,
        Boolean activo
) {
}