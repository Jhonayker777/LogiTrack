package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.ProductoDtoRequest;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.mapper.ProductoMapper;
import LogiTrack.API.Model.Producto;
import LogiTrack.API.repository.DetalleMovimientoRepository;
import LogiTrack.API.repository.ProductoRepository;
import LogiTrack.API.repository.StockBodegaRepository;
import LogiTrack.API.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final StockBodegaRepository stockBodegaRepository;

    @Override
    public ProductoDtoResponse guardar(ProductoDtoRequest dto) {
        if (productoRepository.existsByNombre(dto.nombre()))
            throw new RuntimeException("Ya existe un producto con el nombre: " + dto.nombre());
        Producto p = productoMapper.DtoToEntity(dto);
        Producto productoRespuesta = productoRepository.save(p);
        return productoMapper.entityToDto(productoRespuesta);
    }

    @Override
    public ProductoDtoResponse actualizar(ProductoDtoRequest dto, Long id) {
        Producto p = productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, producto a actualizar no encontrado"));
        productoRepository.findByNombre(dto.nombre()).ifPresent(otro -> {
            if (!otro.getId().equals(id))
                throw new RuntimeException("Ya existe un producto con el nombre: " + dto.nombre());
        });
        productoMapper.updateEntityFromDto(p, dto);
        Producto productoActualizado = productoRepository.save(p);
        return productoMapper.entityToDto(productoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        Producto p = productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, producto a eliminar no existe"));
        if (detalleMovimientoRepository.existsByProductoId(p.getId()))
            throw new RuntimeException("Este producto pertenece a un detalle de movimiento");
        if (stockBodegaRepository.existsByProductoId(p.getId()))
            throw new RuntimeException("Este producto tiene stock registrado en una bodega");
        productoRepository.delete(p);
    }

    @Override
    public List<ProductoDtoResponse> listar() {
        return productoRepository.findAll().stream().map(productoMapper::entityToDto).toList();
    }

    @Override
    public ProductoDtoResponse buscarId(Long id) {
        return productoMapper.entityToDto(productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, no existe producto con ese id")));
    }

    @Override
    public List<ProductoDtoResponse> buscarCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream().map(productoMapper::entityToDto).toList();
    }

    @Override
    public List<ProductoDtoResponse> listarStockBajo() {
        return productoRepository.findProductosStockBajo().stream().map(productoMapper::entityToDto).toList();
    }
}
