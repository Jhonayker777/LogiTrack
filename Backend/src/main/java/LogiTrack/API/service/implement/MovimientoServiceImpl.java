package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.MovimientoDtoRequest;
import LogiTrack.API.dto.response.MovimientoDtoResponse;
import LogiTrack.API.exception.BussinesRuleException;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usernameUsuario));

        Bodega origen = null;
        if (dto.bodegaOrigenId() != null) {
            origen = bodegaRepository.findById(dto.bodegaOrigenId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega de origen no existe"));
            if (!Boolean.TRUE.equals(origen.getActivo()))
                throw new BussinesRuleException("La bodega de origen '" + origen.getNombre() + "' está inactiva.");
        }

        Bodega destino = null;
        if (dto.bodegaDestinoId() != null) {
            destino = bodegaRepository.findById(dto.bodegaDestinoId())
                    .orElseThrow(() -> new EntityNotFoundException("Bodega de destino no existe"));
            if (!Boolean.TRUE.equals(destino.getActivo()))
                throw new BussinesRuleException("La bodega de destino '" + destino.getNombre() + "' está inactiva.");
        }

        // Validaciones lógicas
        if (dto.tipo() == Movimiento.Tipo.ENTRADA && destino == null)
            throw new BussinesRuleException("Una entrada requiere una bodega de destino.");
        if (dto.tipo() == Movimiento.Tipo.SALIDA && origen == null)
            throw new BussinesRuleException("Una salida requiere una bodega de origen.");
        if (dto.tipo() == Movimiento.Tipo.TRANSFERENCIA) {
            if (origen == null || destino == null)
                throw new BussinesRuleException("Una transferencia requiere bodega de origen y destino.");
            if (origen.getId().equals(destino.getId()))
                throw new BussinesRuleException("La bodega de origen y destino no pueden ser la misma.");
        }

        Producto producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no existe"));

        if (!Boolean.TRUE.equals(producto.getActivo()))
            throw new BussinesRuleException("El producto '" + producto.getNombre() + "' está inactivo.");

        // 1. Crear cabecera
        Movimiento movimiento = movimientoMapper.DtoToEntity(dto, usuario, origen, destino);
        movimiento.setFecha(LocalDateTime.now());
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        // 2. Crear Detalle
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

        return movimientoMapper.entityToDto(
                movimientoGuardado,
                usuarioMapper.entityToResumenDto(usuario),
                origen != null ? bodegaMapper.entityToDto(origen, usuarioMapper.entityToResumenDto(origen.getEncargado()), ciudadMapper.entityToDto(origen.getCiudad(), departamentoMapper.entityToDto(origen.getCiudad().getDepartamento()))) : null,
                destino != null ? bodegaMapper.entityToDto(destino, usuarioMapper.entityToResumenDto(destino.getEncargado()), ciudadMapper.entityToDto(destino.getCiudad(), departamentoMapper.entityToDto(destino.getCiudad().getDepartamento()))) : null
        );
    }

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
            throw new BussinesRuleException("Stock insuficiente en '" + bodega.getNombre() +
                    "' para el producto '" + producto.getNombre() + "'. Stock actual: " + stock.getCantidad());
        }

        if (cantidadCambio > 0) {
            Integer totalActualBodega = stockBodegaRepository.totalUnidadesEnBodega(bodega.getId());
            int espacioLibre = bodega.getCapacidad() - totalActualBodega;
            if (cantidadCambio > espacioLibre) {
                throw new BussinesRuleException("Capacidad excedida en '" + bodega.getNombre() +
                        "'. Capacidad: " + bodega.getCapacidad() + ". Ocupado: " + totalActualBodega +
                        ". Libre: " + espacioLibre + ". Intentaste ingresar: " + cantidadCambio);
            }
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
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado con ID: " + id));
        return movimientoMapper.entityToDto(m,
                usuarioMapper.entityToResumenDto(m.getUsuario()),
                m.getBodegaOrigen() != null ? bodegaMapper.entityToDto(m.getBodegaOrigen(), usuarioMapper.entityToResumenDto(m.getBodegaOrigen().getEncargado()), ciudadMapper.entityToDto(m.getBodegaOrigen().getCiudad(), departamentoMapper.entityToDto(m.getBodegaOrigen().getCiudad().getDepartamento()))) : null,
                m.getBodegaDestino() != null ? bodegaMapper.entityToDto(m.getBodegaDestino(), usuarioMapper.entityToResumenDto(m.getBodegaDestino().getEncargado()), ciudadMapper.entityToDto(m.getBodegaDestino().getCiudad(), departamentoMapper.entityToDto(m.getBodegaDestino().getCiudad().getDepartamento()))) : null);
    }
}