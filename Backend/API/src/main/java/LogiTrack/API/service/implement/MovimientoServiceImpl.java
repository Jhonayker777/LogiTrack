package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.DetalleMovimientoDtoRequest;
import LogiTrack.API.dto.request.MovimientoDtoRequest;
import LogiTrack.API.dto.response.MovimientoDtoResponse;
import LogiTrack.API.mapper.BodegaMapper;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.mapper.DetalleMovimientoMapper;
import LogiTrack.API.mapper.MovimientoMapper;
import LogiTrack.API.mapper.ProductoMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.*;
import LogiTrack.API.repository.*;
import LogiTrack.API.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final StockBodegaRepository stockBodegaRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    private final MovimientoMapper movimientoMapper;
    private final DetalleMovimientoMapper detalleMovimientoMapper;
    private final ProductoMapper productoMapper;
    private final BodegaMapper bodegaMapper;
    private final UsuarioMapper usuarioMapper;
    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;

    @Override
    @Transactional
    public MovimientoDtoResponse guardar(MovimientoDtoRequest dto, String usernameUsuario) {
        Usuario usuario = usuarioRepository.findByUsername(usernameUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Bodega origen = null;
        if (dto.bodegaOrigenId() != null) {
            origen = bodegaRepository.findById(dto.bodegaOrigenId())
                    .orElseThrow(() -> new RuntimeException("Bodega de origen no existe"));
        }

        Bodega destino = null;
        if (dto.bodegaDestinoId() != null) {
            destino = bodegaRepository.findById(dto.bodegaDestinoId())
                    .orElseThrow(() -> new RuntimeException("Bodega de destino no existe"));
        }

        // Validaciones
        if (dto.tipo() == Movimiento.Tipo.ENTRADA && destino == null)
            throw new RuntimeException("Una entrada requiere bodega de destino");
        if (dto.tipo() == Movimiento.Tipo.SALIDA && origen == null)
            throw new RuntimeException("Una salida requiere bodega de origen");
        if (dto.tipo() == Movimiento.Tipo.TRANSFERENCIA) {
            if (origen == null || destino == null)
                throw new RuntimeException("Transferencia requiere origen y destino");
            if (origen.getId().equals(destino.getId()))
                throw new RuntimeException("Origen y destino no pueden ser iguales");
        }

        Producto producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        // 1. Crear la cabecera del movimiento
        Movimiento movimiento = movimientoMapper.DtoToEntity(dto, usuario, origen, destino);
        movimiento.setFecha(LocalDateTime.now());
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        // 2. Crear el DetalleMovimiento (ya que no viene en el DTO, lo creamos aquí)
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimiento(movimientoGuardado);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.cantidad());
        detalleMovimientoRepository.save(detalle);

        // 3. Actualizar Stock
        if (dto.tipo() == Movimiento.Tipo.ENTRADA) {
            actualizarStock(destino, producto, dto.cantidad());
        } else if (dto.tipo() == Movimiento.Tipo.SALIDA) {
            actualizarStock(origen, producto, -dto.cantidad());
        } else if (dto.tipo() == Movimiento.Tipo.TRANSFERENCIA) {
            actualizarStock(origen, producto, -dto.cantidad());
            actualizarStock(destino, producto, dto.cantidad());
        }

        // 4. Armar el DTO de respuesta
        // Nota: Como el mapper no recibe detalles, solo armamos los DTOs de las bodegas
        return movimientoMapper.entityToDto(
                movimientoGuardado,
                usuarioMapper.entityToResumenDto(usuario),
                origen != null ? bodegaMapper.entityToDto(origen, usuarioMapper.entityToResumenDto(origen.getEncargado()), ciudadMapper.entityToDto(origen.getCiudad(), departamentoMapper.entityToDto(origen.getCiudad().getDepartamento()))) : null,
                destino != null ? bodegaMapper.entityToDto(destino, usuarioMapper.entityToResumenDto(destino.getEncargado()), ciudadMapper.entityToDto(destino.getCiudad(), departamentoMapper.entityToDto(destino.getCiudad().getDepartamento()))) : null
        );
    }

    // Método auxiliar para buscar o crear el registro en stock_bodega y actualizar cantidad
    private void actualizarStock(Bodega bodega, Producto producto, int cantidadCambio) {
        StockBodega stock = stockBodegaRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElseGet(() -> {
                    StockBodega nuevo = new StockBodega();
                    nuevo.setBodega(bodega);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        int nuevaCantidad = stock.getCantidad() + cantidadCambio;
        if (nuevaCantidad < 0) {
            throw new RuntimeException("Stock insuficiente en la bodega " + bodega.getNombre() + " para el producto " + producto.getNombre());
        }

        stock.setCantidad(nuevaCantidad);
        stockBodegaRepository.save(stock);
    }

    @Override
    public List<MovimientoDtoResponse> listar() {
        return movimientoRepository.findAll().stream().map(
                m -> movimientoMapper.entityToDto(m,
                        usuarioMapper.entityToResumenDto(m.getUsuario()),
                        m.getBodegaOrigen() != null ? bodegaMapper.entityToDto(m.getBodegaOrigen(), usuarioMapper.entityToResumenDto(m.getBodegaOrigen().getEncargado()), ciudadMapper.entityToDto(m.getBodegaOrigen().getCiudad(), departamentoMapper.entityToDto(m.getBodegaOrigen().getCiudad().getDepartamento()))) : null,
                        m.getBodegaDestino() != null ? bodegaMapper.entityToDto(m.getBodegaDestino(), usuarioMapper.entityToResumenDto(m.getBodegaDestino().getEncargado()), ciudadMapper.entityToDto(m.getBodegaDestino().getCiudad(), departamentoMapper.entityToDto(m.getBodegaDestino().getCiudad().getDepartamento()))) : null)
        ).toList();
    }

    @Override
    public MovimientoDtoResponse buscarId(Long id) {
        Movimiento m = movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        return movimientoMapper.entityToDto(m,
                usuarioMapper.entityToResumenDto(m.getUsuario()),
                m.getBodegaOrigen() != null ? bodegaMapper.entityToDto(m.getBodegaOrigen(), usuarioMapper.entityToResumenDto(m.getBodegaOrigen().getEncargado()), ciudadMapper.entityToDto(m.getBodegaOrigen().getCiudad(), departamentoMapper.entityToDto(m.getBodegaOrigen().getCiudad().getDepartamento()))) : null,
                m.getBodegaDestino() != null ? bodegaMapper.entityToDto(m.getBodegaDestino(), usuarioMapper.entityToResumenDto(m.getBodegaDestino().getEncargado()), ciudadMapper.entityToDto(m.getBodegaDestino().getCiudad(), departamentoMapper.entityToDto(m.getBodegaDestino().getCiudad().getDepartamento()))) : null);
    }
}
