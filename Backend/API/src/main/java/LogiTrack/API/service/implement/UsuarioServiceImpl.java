package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.UsuarioDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.UsuarioDtoResponse;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.BodegaRepository;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.MovimientoRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CiudadRepository ciudadRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoRepository movimientoRepository;

    private final UsuarioMapper usuarioMapper;
    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioDtoResponse guardar(UsuarioDtoRequest dto) {
        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("El username ya está en uso");
        }

        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new RuntimeException("La ciudad no existe"));

        // Encriptamos la contraseña
        String passwordHasheado = passwordEncoder.encode("password123");
        // NOTA: Como el DTO de usuario no tiene password (por seguridad),
        // aquí ponemos una por defecto.

        Usuario usuario = usuarioMapper.DtoToEntity(dto, ciudad, passwordHasheado);
        usuario.setFechaCreacion(LocalDateTime.now());
        Usuario guardado = usuarioRepository.save(usuario);

        CiudadDtoResponse ciudadDto = ciudadMapper.entityToDto(guardado.getCiudad(),
                departamentoMapper.entityToDto(guardado.getCiudad().getDepartamento()));

        return usuarioMapper.entityToDto(guardado, ciudadDto);
    }

    @Override
    public UsuarioDtoResponse actualizar(UsuarioDtoRequest dto, Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new RuntimeException("La ciudad no existe"));

        usuarioMapper.updateEntityFromDto(usuario, dto, ciudad);
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario actualizado = usuarioRepository.save(usuario);

        CiudadDtoResponse ciudadDto = ciudadMapper.entityToDto(actualizado.getCiudad(),
                departamentoMapper.entityToDto(actualizado.getCiudad().getDepartamento()));

        return usuarioMapper.entityToDto(actualizado, ciudadDto);
    }

    @Override
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        if (bodegaRepository.existsByEncargadoId(usuario.getId()))
            throw new RuntimeException("Este usuario es encargado de una bodega");
        if (movimientoRepository.existsByUsuarioId(usuario.getId()))
            throw new RuntimeException("Este usuario tiene movimientos registrados");
        usuarioRepository.delete(usuario);
    }

    @Override
    public List<UsuarioDtoResponse> listar() {
        return usuarioRepository.findAll().stream().map(
                u -> usuarioMapper.entityToDto(u,
                        ciudadMapper.entityToDto(u.getCiudad(), departamentoMapper.entityToDto(u.getCiudad().getDepartamento())))
        ).toList();
    }

    @Override
    public UsuarioDtoResponse buscarId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        return usuarioMapper.entityToDto(usuario,
                ciudadMapper.entityToDto(usuario.getCiudad(), departamentoMapper.entityToDto(usuario.getCiudad().getDepartamento())));
    }
}