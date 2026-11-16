package LogiTrack.API.dto.response;

public record DetalleMovimientoDtoResponse(
        Long id,
        ProductoDtoResponse producto,
        Integer cantidad
) {
}