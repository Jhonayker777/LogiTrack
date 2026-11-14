package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.RechazoDtoRequest;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.mapper.SolicitudRegistroMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.SolicitudRegistro;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.SolicitudRegistroRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.SolicitudRegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudRegistroServiceImpl implements SolicitudRegistroService {

    private final SolicitudRegistroRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final CiudadRepository ciudadRepository;

    private final SolicitudRegistroMapper solicitudMapper;
    private final UsuarioMapper usuarioMapper;
    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public SolicitudRegistroDtoResponse guardar(SolicitudRegistroDtoRequest dto) {
        if (usuarioRepository.existsByUsername(dto.username()) || usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("El username o email ya están registrados en el sistema.");
        }

        if (solicitudRepository.existsByUsernameAndEstado(dto.username(), SolicitudRegistro.Estado.PENDIENTE) ||
                solicitudRepository.existsByEmailAndEstado(dto.email(), SolicitudRegistro.Estado.PENDIENTE)) {
            throw new RuntimeException("Ya existe una solicitud pendiente con este username o email.");
        }

        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new RuntimeException("La ciudad no existe"));

        String passwordHasheado = passwordEncoder.encode(dto.password());

        SolicitudRegistro solicitud = solicitudMapper.DtoToEntity(dto, ciudad, passwordHasheado);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        SolicitudRegistro guardada = solicitudRepository.save(solicitud);

        CiudadDtoResponse ciudadDto = ciudadMapper.entityToDto(guardada.getCiudad(),
                departamentoMapper.entityToDto(guardada.getCiudad().getDepartamento()));

        return solicitudMapper.entityToDto(guardada, ciudadDto, null, null);
    }

    @Override
    @Transactional
    public SolicitudRegistroDtoResponse aprobar(Long id, String usernameAdmin) {
        SolicitudRegistro solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        if (solicitud.getEstado() != SolicitudRegistro.Estado.PENDIENTE) {
            throw new RuntimeException("La solicitud ya fue resuelta (Estado actual: " + solicitud.getEstado() + ")");
        }

        Usuario admin = usuarioRepository.findByUsername(usernameAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // 1. Crear el Usuario real copiando los datos de la solicitud
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(solicitud.getNombre());
        nuevoUsuario.setApellido(solicitud.getApellido());
        nuevoUsuario.setDocumento(solicitud.getDocumento());
        nuevoUsuario.setEmail(solicitud.getEmail());
        nuevoUsuario.setTelefono(solicitud.getTelefono());
        nuevoUsuario.setDireccion(solicitud.getDireccion());
        nuevoUsuario.setCiudad(solicitud.getCiudad());
        nuevoUsuario.setUsername(solicitud.getUsername());
        nuevoUsuario.setPassword(solicitud.getPassword());
        nuevoUsuario.setRol(solicitud.getRolSolicitado());
        nuevoUsuario.setTurno(solicitud.getTurno());
        nuevoUsuario.setFechaIngreso(solicitud.getFechaIngreso());
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 2. Actualizar la solicitud
        solicitud.setEstado(SolicitudRegistro.Estado.APROBADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitud.setResueltoPor(admin);
        solicitud.setUsuarioCreado(usuarioGuardado);
        SolicitudRegistro actualizada = solicitudRepository.save(solicitud);

        // 3. Preparar los DTOs para el response
        CiudadDtoResponse ciudadDto = ciudadMapper.entityToDto(actualizada.getCiudad(),
                departamentoMapper.entityToDto(actualizada.getCiudad().getDepartamento()));
        UsuarioResumenDtoResponse adminDto = usuarioMapper.entityToResumenDto(admin);
        UsuarioResumenDtoResponse nuevoUsuarioDto = usuarioMapper.entityToResumenDto(usuarioGuardado);

        return solicitudMapper.entityToDto(actualizada, ciudadDto, adminDto, nuevoUsuarioDto);
    }

    @Override
    public SolicitudRegistroDtoResponse rechazar(Long id, RechazoDtoRequest dto, String usernameAdmin) {
        SolicitudRegistro solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        if (solicitud.getEstado() != SolicitudRegistro.Estado.PENDIENTE) {
            throw new RuntimeException("La solicitud ya fue resuelta");
        }

        Usuario admin = usuarioRepository.findByUsername(usernameAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        solicitud.setEstado(SolicitudRegistro.Estado.RECHAZADA);
        solicitud.setMotivoRechazo(dto.motivo());
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitud.setResueltoPor(admin);

        SolicitudRegistro actualizada = solicitudRepository.save(solicitud);

        CiudadDtoResponse ciudadDto = ciudadMapper.entityToDto(actualizada.getCiudad(),
                departamentoMapper.entityToDto(actualizada.getCiudad().getDepartamento()));
        UsuarioResumenDtoResponse adminDto = usuarioMapper.entityToResumenDto(admin);

        return solicitudMapper.entityToDto(actualizada, ciudadDto, adminDto, null);
    }

    @Override
    public List<SolicitudRegistroDtoResponse> listar() {
        return solicitudRepository.findAll().stream().map(
                s -> solicitudMapper.entityToDto(s,
                        ciudadMapper.entityToDto(s.getCiudad(), departamentoMapper.entityToDto(s.getCiudad().getDepartamento())),
                        s.getResueltoPor() != null ? usuarioMapper.entityToResumenDto(s.getResueltoPor()) : null,
                        s.getUsuarioCreado() != null ? usuarioMapper.entityToResumenDto(s.getUsuarioCreado()) : null)
        ).toList();
    }

    @Override
    public List<SolicitudRegistroDtoResponse> buscarEstado(String estado) {
        SolicitudRegistro.Estado estadoEnum;
        try {
            estadoEnum = SolicitudRegistro.Estado.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + estado + ". Valores permitidos: PENDIENTE, APROBADA, RECHAZADA");
        }
        return solicitudRepository.findByEstado(estadoEnum).stream().map(
                s -> solicitudMapper.entityToDto(s,
                        ciudadMapper.entityToDto(s.getCiudad(), departamentoMapper.entityToDto(s.getCiudad().getDepartamento())),
                        s.getResueltoPor() != null ? usuarioMapper.entityToResumenDto(s.getResueltoPor()) : null,
                        s.getUsuarioCreado() != null ? usuarioMapper.entityToResumenDto(s.getUsuarioCreado()) : null)
        ).toList();
    }

    @Override
    public SolicitudRegistroDtoResponse buscarId(Long id) {
        SolicitudRegistro solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        return solicitudMapper.entityToDto(solicitud,
                ciudadMapper.entityToDto(solicitud.getCiudad(), departamentoMapper.entityToDto(solicitud.getCiudad().getDepartamento())),
                solicitud.getResueltoPor() != null ? usuarioMapper.entityToResumenDto(solicitud.getResueltoPor()) : null,
                solicitud.getUsuarioCreado() != null ? usuarioMapper.entityToResumenDto(solicitud.getUsuarioCreado()) : null);
    }
}