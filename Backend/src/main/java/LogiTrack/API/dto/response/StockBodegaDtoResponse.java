package LogiTrack.API.dto.response;

public record StockBodegaDtoResponse(
        Long id,
        BodegaDtoResponse bodega,
        ProductoDtoResponse producto,
        Integer cantidad
) {
}