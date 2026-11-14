package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.StockBodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.StockBodegaDtoResponse;
import LogiTrack.API.mapper.BodegaMapper;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.mapper.ProductoMapper;
import LogiTrack.API.mapper.StockBodegaMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.Producto;
import LogiTrack.API.Model.StockBodega;
import LogiTrack.API.repository.BodegaRepository;
import LogiTrack.API.repository.ProductoRepository;
import LogiTrack.API.repository.StockBodegaRepository;
import LogiTrack.API.service.StockBodegaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBodegaServiceImpl implements StockBodegaService {

    private final StockBodegaRepository stockRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;

    private final StockBodegaMapper stockMapper;
    private final BodegaMapper bodegaMapper;
    private final ProductoMapper productoMapper;
    private final UsuarioMapper usuarioMapper;
    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;

    @Override
    public List<StockBodegaDtoResponse> listar() {
        return stockRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<StockBodegaDtoResponse> buscarBodegaId(Long bodegaId) {
        if (!bodegaRepository.existsById(bodegaId)) {
            throw new RuntimeException("Bodega no encontrada con ID: " + bodegaId);
        }
        return stockRepository.findByBodegaId(bodegaId).stream().map(this::toDto).toList();
    }

    @Override
    public List<StockBodegaDtoResponse> buscarProductoId(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new RuntimeException("Producto no encontrado con ID: " + productoId);
        }
        return stockRepository.findByProductoId(productoId).stream().map(this::toDto).toList();
    }

    @Override
    public StockBodegaDtoResponse buscarId(Long id) {
        return toDto(buscarEntidad(id));
    }

    @Override
    public StockBodegaDtoResponse buscarBodegaProducto(Long bodegaId, Long productoId) {
        StockBodega stock = stockRepository.findByBodegaIdAndProductoId(bodegaId, productoId)
                .orElseThrow(() -> new RuntimeException(
                        "No hay stock registrado del producto " + productoId + " en la bodega " + bodegaId));
        return toDto(stock);
    }

    @Override
    public Integer stockTotalProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new RuntimeException("Producto no encontrado con ID: " + productoId);
        }
        return productoRepository.stockTotalByProducto(productoId);
    }

    @Override
    public StockBodegaDtoResponse guardar(StockBodegaDtoRequest dto) {
        if (stockRepository.existsByBodegaIdAndProductoId(dto.bodegaId(), dto.productoId())) {
            throw new RuntimeException("Ya existe un registro de stock para ese producto en esa bodega. Use la actualización.");
        }

        Bodega bodega = buscarBodega(dto.bodegaId());
        Producto producto = buscarProducto(dto.productoId());

        StockBodega stock = stockMapper.DtoToEntity(dto, bodega, producto);
        return toDto(stockRepository.save(stock));
    }

    @Override
    public StockBodegaDtoResponse actualizar(StockBodegaDtoRequest dto, Long id) {
        StockBodega stock = buscarEntidad(id);

        // Si cambia el par bodega-producto, no debe chocar con otro registro existente
        stockRepository.findByBodegaIdAndProductoId(dto.bodegaId(), dto.productoId()).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new RuntimeException("Ya existe un registro de stock para ese producto en esa bodega");
            }
        });

        Bodega bodega = buscarBodega(dto.bodegaId());
        Producto producto = buscarProducto(dto.productoId());

        stockMapper.updateEntityFromDto(stock, dto, bodega, producto);
        return toDto(stockRepository.save(stock));
    }

    @Override
    public void eliminar(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new RuntimeException("Registro de stock no encontrado con ID: " + id);
        }
        stockRepository.deleteById(id);
    }

    // ── Helpers ──

    private StockBodega buscarEntidad(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de stock no encontrado con ID: " + id));
    }

    private Bodega buscarBodega(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La bodega no existe"));
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El producto no existe"));
    }

    private StockBodegaDtoResponse toDto(StockBodega s) {
        Bodega b = s.getBodega();
        CiudadDtoResponse ciudadDto = b.getCiudad() == null ? null
                : ciudadMapper.entityToDto(b.getCiudad(), departamentoMapper.entityToDto(b.getCiudad().getDepartamento()));
        BodegaDtoResponse bodegaDto = bodegaMapper.entityToDto(b, usuarioMapper.entityToResumenDto(b.getEncargado()), ciudadDto);
        return stockMapper.entityToDto(s, bodegaDto, productoMapper.entityToDto(s.getProducto()));
    }
}
