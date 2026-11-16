package LogiTrack.API.service.implement;

import LogiTrack.API.dto.response.AuditoriaDtoResponse;
import LogiTrack.API.mapper.AuditoriaMapper;
import LogiTrack.API.mapper.UsuarioMapper;
import LogiTrack.API.Model.Auditoria;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.AuditoriaRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    private final AuditoriaMapper auditoriaMapper;
    private final UsuarioMapper usuarioMapper;

    @Override
    public void registrar(Auditoria.Operacion operacion, String usernameUsuario, String entidadAfectada,
                          Long registroId, String valoresAnteriores, String valoresNuevos,
                          String ipOrigen, String userAgent) {
        // El usuario es opcional (p. ej. acciones anónimas como solicitar registro)
        Usuario usuario = usernameUsuario == null ? null
                : usuarioRepository.findByUsername(usernameUsuario).orElse(null);

        Auditoria auditoria = Auditoria.builder()
                .operacion(operacion)
                .fechaHora(LocalDateTime.now())
                .usuario(usuario)
                .entidadAfectada(entidadAfectada)
                .registroId(registroId)
                .valoresAnteriores(valoresAnteriores)
                .valoresNuevos(valoresNuevos)
                .ipOrigen(ipOrigen)
                .userAgent(userAgent)
                .build();

        auditoriaRepository.save(auditoria);
    }

    @Override
    public List<AuditoriaDtoResponse> listar() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc().stream().map(this::toDto).toList();
    }

    @Override
    public AuditoriaDtoResponse buscarId(Long id) {
        Auditoria auditoria = auditoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de auditoría no encontrado con ID: " + id));
        return toDto(auditoria);
    }

    @Override
    public List<AuditoriaDtoResponse> buscarEntidad(String entidad) {
        return auditoriaRepository.findByEntidadAfectadaOrderByFechaHoraDesc(entidad).stream().map(this::toDto).toList();
    }

    @Override
    public List<AuditoriaDtoResponse> buscarRegistro(String entidad, Long registroId) {
        return auditoriaRepository.findByEntidadAfectadaAndRegistroIdOrderByFechaHoraDesc(entidad, registroId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<AuditoriaDtoResponse> buscarUsuarioId(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId).stream().map(this::toDto).toList();
    }

    @Override
    public List<AuditoriaDtoResponse> buscarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta)) {
            throw new RuntimeException("La fecha inicial no puede ser posterior a la final");
        }
        return auditoriaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta)
                .stream().map(this::toDto).toList();
    }

    // ── Helper ──

    private AuditoriaDtoResponse toDto(Auditoria a) {
        return auditoriaMapper.entityToDto(a, usuarioMapper.entityToResumenDto(a.getUsuario()));
    }
}
