package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.UsuarioDtoRequest;
import LogiTrack.API.dto.request.PerfilUsuarioDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.UsuarioDtoResponse;
import LogiTrack.API.exception.BussinesRuleException;
import LogiTrack.API.exception.DatabaseRuleException;
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
import jakarta.persistence.EntityNotFoundException;
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
            throw new BussinesRuleException("El username ya está en uso: " + dto.username());
        }
        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new EntityNotFoundException("La ciudad no existe con ID: " + dto.ciudadId()));

        String passwordHasheado = passwordEncoder.encode("password123");
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
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new EntityNotFoundException("La ciudad no existe con ID: " + dto.ciudadId()));

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
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getRol() == Usuario.Rol.ADMIN && Boolean.TRUE.equals(usuario.getActivo())) {
            long adminsActivos = usuarioRepository.countByRolAndActivoTrue(Usuario.Rol.ADMIN);
            if (adminsActivos <= 1) {
                throw new BussinesRuleException("No se puede eliminar al último administrador activo del sistema.");
            }
        }

        if (bodegaRepository.existsByEncargadoId(usuario.getId()))
            throw new DatabaseRuleException("No se puede eliminar el usuario porque es encargado de una bodega.");
        if (movimientoRepository.existsByUsuarioId(usuario.getId()))
            throw new DatabaseRuleException("No se puede eliminar el usuario porque tiene movimientos registrados.");
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
    public UsuarioDtoResponse buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
        CiudadDtoResponse ciudadDto = usuario.getCiudad() == null ? null : ciudadMapper.entityToDto(
                usuario.getCiudad(), departamentoMapper.entityToDto(usuario.getCiudad().getDepartamento()));
        return usuarioMapper.entityToDto(usuario, ciudadDto);
    }

    @Override
    public UsuarioDtoResponse actualizarPerfil(String username, PerfilUsuarioDtoRequest dto) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
        Ciudad ciudad = dto.ciudadId() == null ? null : ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new EntityNotFoundException("La ciudad seleccionada no existe"));

        usuario.setNombre(dto.nombre().trim());
        usuario.setApellido(dto.apellido().trim());
        usuario.setDocumento(dto.documento());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setCiudad(ciudad);
        // username, password, rol, turno, fechaIngreso y activo permanecen intactos.
        usuario.setFechaActualizacion(LocalDateTime.now());
        Usuario actualizado = usuarioRepository.save(usuario);
        CiudadDtoResponse ciudadDto = actualizado.getCiudad() == null ? null : ciudadMapper.entityToDto(
                actualizado.getCiudad(), departamentoMapper.entityToDto(actualizado.getCiudad().getDepartamento()));
        return usuarioMapper.entityToDto(actualizado, ciudadDto);
    }

    @Override
    public UsuarioDtoResponse buscarId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        return usuarioMapper.entityToDto(usuario,
                ciudadMapper.entityToDto(usuario.getCiudad(), departamentoMapper.entityToDto(usuario.getCiudad().getDepartamento())));
    }
}