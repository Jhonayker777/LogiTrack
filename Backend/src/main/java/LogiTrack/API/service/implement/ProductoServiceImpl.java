package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.ProductoDtoRequest;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.exception.BussinesRuleException;
import LogiTrack.API.exception.DatabaseRuleException;
import LogiTrack.API.mapper.ProductoMapper;
import LogiTrack.API.Model.Producto;
import LogiTrack.API.repository.DetalleMovimientoRepository;
import LogiTrack.API.repository.ProductoRepository;
import LogiTrack.API.repository.StockBodegaRepository;
import LogiTrack.API.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
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
            throw new BussinesRuleException("Ya existe un producto con el nombre: " + dto.nombre());

        Producto p = productoMapper.DtoToEntity(dto);
        Producto productoRespuesta = productoRepository.save(p);
        return productoMapper.entityToDto(productoRespuesta);
    }

    @Override
    public ProductoDtoResponse actualizar(ProductoDtoRequest dto, Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto a actualizar no encontrado con ID: " + id));

        productoRepository.findByNombre(dto.nombre()).ifPresent(otro -> {
            if (!otro.getId().equals(id))
                throw new BussinesRuleException("Ya existe un producto con el nombre: " + dto.nombre());
        });

        productoMapper.updateEntityFromDto(p, dto);
        Producto productoActualizado = productoRepository.save(p);
        return productoMapper.entityToDto(productoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto a eliminar no existe con ID: " + id));

        if (detalleMovimientoRepository.existsByProductoId(p.getId()))
            throw new DatabaseRuleException("No se puede eliminar el producto porque pertenece a un movimiento registrado.");
        if (stockBodegaRepository.existsByBodegaId(p.getId()))
            throw new DatabaseRuleException("No se puede eliminar el producto porque tiene stock registrado en una bodega.");

        productoRepository.delete(p);
    }

    @Override
    public List<ProductoDtoResponse> listar() {
        return productoRepository.findAll().stream().map(productoMapper::entityToDto).toList();
    }

    @Override
    public ProductoDtoResponse buscarId(Long id) {
        return productoMapper.entityToDto(productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe producto con el ID: " + id)));
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