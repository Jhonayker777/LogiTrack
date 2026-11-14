package LogiTrack.API.service;

import LogiTrack.API.dto.request.StockBodegaDtoRequest;
import LogiTrack.API.dto.response.StockBodegaDtoResponse;

import java.util.List;

public interface StockBodegaService {
    // Alta / ajuste manual de inventario (los movimientos lo actualizan automáticamente)
    StockBodegaDtoResponse guardar(StockBodegaDtoRequest dto);

    StockBodegaDtoResponse actualizar(StockBodegaDtoRequest dto, Long id);

    void eliminar(Long id);

    List<StockBodegaDtoResponse> listar();

    StockBodegaDtoResponse buscarId(Long id);

    List<StockBodegaDtoResponse> buscarBodegaId(Long id);

    List<StockBodegaDtoResponse> buscarProductoId(Long id);

    StockBodegaDtoResponse buscarBodegaProducto(Long bodegaId, Long productoId);

    Integer stockTotalProducto(Long productoId);
}
