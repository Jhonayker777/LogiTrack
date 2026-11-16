package LogiTrack.API.service;

import LogiTrack.API.dto.request.ProductoDtoRequest;
import LogiTrack.API.dto.response.ProductoDtoResponse;

import java.util.List;

public interface ProductoService {
    ProductoDtoResponse guardar(ProductoDtoRequest dto);

    ProductoDtoResponse actualizar(ProductoDtoRequest dto, Long id);

    void eliminar(Long id);

    List<ProductoDtoResponse> listar();

    ProductoDtoResponse buscarId(Long id);

    List<ProductoDtoResponse> buscarCategoria(String categoria);

    List<ProductoDtoResponse> listarStockBajo();
}
