package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.ProductoDtoRequest;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.Model.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoDtoResponse entityToDto(Producto p) {
        if (p == null) return null;
        return new ProductoDtoResponse(
                p.getId(),
                p.getNombre(),
                p.getCategoria(),
                p.getPrecio(),
                p.getStockMinimo(),
                p.getActivo()
        );
    }

    public Producto DtoToEntity(ProductoDtoRequest dto) {
        if (dto == null) return null;
        Producto p = new Producto();
        p.setNombre(dto.nombre());
        p.setCategoria(dto.categoria());
        p.setPrecio(dto.precio());
        p.setStockMinimo(dto.stockMinimo());
        p.setActivo(true);
        return p;
    }

    public void updateEntityFromDto(Producto p, ProductoDtoRequest dto) {
        if (p == null || dto == null) return;
        p.setNombre(dto.nombre());
        p.setCategoria(dto.categoria());
        p.setPrecio(dto.precio());
        p.setStockMinimo(dto.stockMinimo());
    }
}