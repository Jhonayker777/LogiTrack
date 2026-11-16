package LogiTrack.API.dto.response;

public record CiudadDtoResponse(
        Long id,
        String nombre,
        String codigoPostal,
        DepartamentoDtoResponse departamento
) {
}