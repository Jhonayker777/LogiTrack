package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.DetalleMovimientoDtoRequest;
import LogiTrack.API.dto.response.DetalleMovimientoDtoResponse;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.Model.DetalleMovimiento;
import LogiTrack.API.Model.Movimiento;
import LogiTrack.API.Model.Producto;
import org.springframework.stereotype.Component;

@Component
public class DetalleMovimientoMapper {

    public DetalleMovimientoDtoResponse entityToDto(DetalleMovimiento d, ProductoDtoResponse productoDto) {
        if (d == null || productoDto == null) return null;
        return new DetalleMovimientoDtoResponse(
                d.getId(),
                productoDto,
                d.getCantidad()
        );
    }

    public DetalleMovimiento DtoToEntity(DetalleMovimientoDtoRequest dto, Movimiento movimiento, Producto producto) {
        if (dto == null || movimiento == null || producto == null) return null;
        DetalleMovimiento d = new DetalleMovimiento();
        d.setMovimiento(movimiento);
        d.setProducto(producto);
        d.setCantidad(dto.cantidad());
        return d;
    }

    public void updateEntityFromDto(DetalleMovimiento d, DetalleMovimientoDtoRequest dto, Movimiento movimiento, Producto producto) {
        if (d == null || dto == null || movimiento == null || producto == null) return;
        d.setMovimiento(movimiento);
        d.setProducto(producto);
        d.setCantidad(dto.cantidad());
    }
}