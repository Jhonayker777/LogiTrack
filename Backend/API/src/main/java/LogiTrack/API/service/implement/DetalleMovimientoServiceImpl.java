package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.DetalleMovimientoDtoRequest;
import LogiTrack.API.dto.response.DetalleMovimientoDtoResponse;
import LogiTrack.API.mapper.DetalleMovimientoMapper;
import LogiTrack.API.mapper.ProductoMapper;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.DetalleMovimiento;
import LogiTrack.API.Model.Movimiento;
import LogiTrack.API.Model.Producto;
import LogiTrack.API.Model.StockBodega;
import LogiTrack.API.repository.DetalleMovimientoRepository;
import LogiTrack.API.repository.MovimientoRepository;
import LogiTrack.API.repository.ProductoRepository;
import LogiTrack.API.repository.StockBodegaRepository;
import LogiTrack.API.service.DetalleMovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleMovimientoServiceImpl implements DetalleMovimientoService {

    private final DetalleMovimientoMapper detalleMovimientoMapper;
    private final ProductoMapper productoMapper;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final StockBodegaRepository stockBodegaRepository;

    @Override
    @Transactional
    public DetalleMovimientoDtoResponse guardar(DetalleMovimientoDtoRequest dto) {
        Movimiento m = movimientoRepository.findById(dto.movimientoId()).orElseThrow(() -> new RuntimeException("Codigo de movimiento no existe"));
        Producto p = productoRepository.findById(dto.productoId()).orElseThrow(() -> new RuntimeException("Codigo de producto no existe"));
        DetalleMovimiento detalle = detalleMovimientoMapper.DtoToEntity(dto, m, p);
        aplicarStock(m, p, dto.cantidad());
        return detalleMovimientoMapper.entityToDto(detalleMovimientoRepository.save(detalle), productoMapper.entityToDto(p));
    }

    @Override
    @Transactional
    public DetalleMovimientoDtoResponse actualizar(DetalleMovimientoDtoRequest dto, Long id) {
        DetalleMovimiento detalle = detalleMovimientoRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe el codigo del detalle de movimiento"));
        Movimiento m = movimientoRepository.findById(dto.movimientoId()).orElseThrow(() -> new RuntimeException("Codigo de movimiento no existe"));
        Producto p = productoRepository.findById(dto.productoId()).orElseThrow(() -> new RuntimeException("Codigo de producto no existente"));

        // 1. Revertir el efecto anterior en el stock, 2. aplicar el nuevo
        aplicarStock(detalle.getMovimiento(), detalle.getProducto(), -detalle.getCantidad());
        detalleMovimientoMapper.updateEntityFromDto(detalle, dto, m, p);
        aplicarStock(m, p, dto.cantidad());

        return detalleMovimientoMapper.entityToDto(detalleMovimientoRepository.save(detalle), productoMapper.entityToDto(p));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        DetalleMovimiento detalle = detalleMovimientoRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe dicho detalle de movimiento"));
        aplicarStock(detalle.getMovimiento(), detalle.getProducto(), -detalle.getCantidad());
        detalleMovimientoRepository.delete(detalle);
    }

    @Override
    public List<DetalleMovimientoDtoResponse> listar() {
        return detalleMovimientoRepository.findAll().stream().map(
                dm -> detalleMovimientoMapper.entityToDto(dm, productoMapper.entityToDto(dm.getProducto()))).toList();
    }

    @Override
    public DetalleMovimientoDtoResponse buscarId(Long id) {
        DetalleMovimiento dm = detalleMovimientoRepository.findById(id).orElseThrow(() -> new RuntimeException("No existe dicho ID de detalle de movimiento"));
        return detalleMovimientoMapper.entityToDto(dm, productoMapper.entityToDto(dm.getProducto()));
    }

    @Override
    public List<DetalleMovimientoDtoResponse> buscarMovimientoId(Long id) {
        if (!movimientoRepository.existsById(id))
            throw new RuntimeException("Codigo de movimiento no existe");
        return detalleMovimientoRepository.findByMovimientoId(id).stream().map(
                dm -> detalleMovimientoMapper.entityToDto(dm, productoMapper.entityToDto(dm.getProducto()))).toList();
    }

    @Override
    public List<DetalleMovimientoDtoResponse> buscarProductoId(Long id) {
        if (!productoRepository.existsById(id))
            throw new RuntimeException("Codigo de producto no existe");
        return detalleMovimientoRepository.findByProductoId(id).stream().map(
                dm -> detalleMovimientoMapper.entityToDto(dm, productoMapper.entityToDto(dm.getProducto()))).toList();
    }

    // ── Stock: cantidad positiva aplica el movimiento, negativa lo revierte ──

    private void aplicarStock(Movimiento m, Producto p, int cantidad) {
        switch (m.getTipo()) {
            case ENTRADA -> cambiarStock(m.getBodegaDestino(), p, cantidad);
            case SALIDA -> cambiarStock(m.getBodegaOrigen(), p, -cantidad);
            case TRANSFERENCIA -> {
                cambiarStock(m.getBodegaOrigen(), p, -cantidad);
                cambiarStock(m.getBodegaDestino(), p, cantidad);
            }
        }
    }

    private void cambiarStock(Bodega bodega, Producto producto, int cambio) {
        if (bodega == null)
            throw new RuntimeException("El movimiento no tiene la bodega necesaria para su tipo");

        StockBodega stock = stockBodegaRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElseGet(() -> {
                    StockBodega nuevo = new StockBodega();
                    nuevo.setBodega(bodega);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        int nuevaCantidad = stock.getCantidad() + cambio;
        if (nuevaCantidad < 0)
            throw new RuntimeException("Stock insuficiente en la bodega " + bodega.getNombre() + " para el producto " + producto.getNombre());

        stock.setCantidad(nuevaCantidad);
        stockBodegaRepository.save(stock);
    }
}
