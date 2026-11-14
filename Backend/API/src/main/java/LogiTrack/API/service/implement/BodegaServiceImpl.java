package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.BodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.mapper.BodegaMapper;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.BodegaRepository;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.MovimientoRepository;
import LogiTrack.API.repository.StockBodegaRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.BodegaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BodegaServiceImpl implements BodegaService {

    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CiudadRepository ciudadRepository;
    private final StockBodegaRepository stockBodegaRepository;
    private final MovimientoRepository movimientoRepository;

    private final BodegaMapper bodegaMapper;
    private final UsuarioMapper usuarioMapper;
    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;

    @Override
    public BodegaDtoResponse guardar(BodegaDtoRequest dto) {
        if (bodegaRepository.existsByNombre(dto.nombre())) {
            throw new RuntimeException("Ya existe una bodega con el nombre: " + dto.nombre());
        }

        Usuario encargado = buscarEncargado(dto.encargadoId());
        Ciudad ciudad = buscarCiudad(dto.ciudadId());

        Bodega bodega = bodegaMapper.DtoToEntity(dto, encargado, ciudad);
        bodega.setFechaCreacion(LocalDateTime.now());

        return toDto(bodegaRepository.save(bodega));
    }

    @Override
    public BodegaDtoResponse actualizar(BodegaDtoRequest dto, Long id) {
        Bodega bodega = bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con ID: " + id));

        bodegaRepository.findByNombre(dto.nombre()).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new RuntimeException("Ya existe una bodega con el nombre: " + dto.nombre());
            }
        });

        Usuario encargado = buscarEncargado(dto.encargadoId());
        Ciudad ciudad = buscarCiudad(dto.ciudadId());

        bodegaMapper.updateEntityFromDto(bodega, dto, encargado, ciudad);
        bodega.setFechaActualizacion(LocalDateTime.now());

        return toDto(bodegaRepository.save(bodega));
    }

    @Override
    public void eliminar(Long id) {
        Bodega bodega = bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con ID: " + id));
        if (stockBodegaRepository.existsByBodegaId(bodega.getId()))
            throw new RuntimeException("Esta bodega tiene stock registrado");
        if (movimientoRepository.existsByBodegaOrigenIdOrBodegaDestinoId(bodega.getId(), bodega.getId()))
            throw new RuntimeException("Esta bodega pertenece a un movimiento");
        bodegaRepository.delete(bodega);
    }

    @Override
    public List<BodegaDtoResponse> listar() {
        return bodegaRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public BodegaDtoResponse buscarId(Long id) {
        Bodega bodega = bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada con ID: " + id));
        return toDto(bodega);
    }

    @Override
    public List<BodegaDtoResponse> buscarCiudadId(Long id) {
        return bodegaRepository.findByCiudadId(id).stream().map(this::toDto).toList();
    }

    @Override
    public List<BodegaDtoResponse> buscarEncargadoId(Long id) {
        return bodegaRepository.findByEncargadoId(id).stream().map(this::toDto).toList();
    }

    // ── Helpers ──

    private Usuario buscarEncargado(Long encargadoId) {
        if (encargadoId == null) return null;
        return usuarioRepository.findById(encargadoId)
                .orElseThrow(() -> new RuntimeException("El usuario encargado no existe"));
    }

    private Ciudad buscarCiudad(Long ciudadId) {
        if (ciudadId == null) return null;
        return ciudadRepository.findById(ciudadId)
                .orElseThrow(() -> new RuntimeException("La ciudad no existe"));
    }

    private BodegaDtoResponse toDto(Bodega b) {
        UsuarioResumenDtoResponse encargadoDto = usuarioMapper.entityToResumenDto(b.getEncargado());
        CiudadDtoResponse ciudadDto = b.getCiudad() == null ? null
                : ciudadMapper.entityToDto(b.getCiudad(), departamentoMapper.entityToDto(b.getCiudad().getDepartamento()));
        return bodegaMapper.entityToDto(b, encargadoDto, ciudadDto);
    }
}
