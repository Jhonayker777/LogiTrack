package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.StockBodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.dto.response.StockBodegaDtoResponse;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.Producto;
import LogiTrack.API.Model.StockBodega;
import org.springframework.stereotype.Component;

@Component
public class StockBodegaMapper {

    public StockBodegaDtoResponse entityToDto(StockBodega s, BodegaDtoResponse bodegaDto, ProductoDtoResponse productoDto) {
        if (s == null) return null;
        return new StockBodegaDtoResponse(
                s.getId(),
                bodegaDto,
                productoDto,
                s.getCantidad()
        );
    }

    public StockBodega DtoToEntity(StockBodegaDtoRequest dto, Bodega bodega, Producto producto) {
        if (dto == null || bodega == null || producto == null) return null;
        StockBodega s = new StockBodega();
        s.setBodega(bodega);
        s.setProducto(producto);
        s.setCantidad(dto.cantidad());
        return s;
    }

    public void updateEntityFromDto(StockBodega s, StockBodegaDtoRequest dto, Bodega bodega, Producto producto) {
        if (s == null || dto == null || bodega == null || producto == null) return;
        s.setBodega(bodega);
        s.setProducto(producto);
        s.setCantidad(dto.cantidad());
    }
}