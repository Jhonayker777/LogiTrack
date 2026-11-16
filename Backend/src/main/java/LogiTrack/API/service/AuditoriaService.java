package LogiTrack.API.service;

import LogiTrack.API.dto.response.AuditoriaDtoResponse;
import LogiTrack.API.Model.Auditoria;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaService {

    // Para uso interno de otros servicios / filtros
    void registrar(Auditoria.Operacion operacion, String usernameUsuario, String entidadAfectada,
                   Long registroId, String valoresAnteriores, String valoresNuevos,
                   String ipOrigen, String userAgent);

    // Consultas (solo ADMIN)
    List<AuditoriaDtoResponse> listar();

    AuditoriaDtoResponse buscarId(Long id);

    List<AuditoriaDtoResponse> buscarEntidad(String entidad);

    List<AuditoriaDtoResponse> buscarRegistro(String entidad, Long registroId);

    List<AuditoriaDtoResponse> buscarUsuarioId(Long usuarioId);

    List<AuditoriaDtoResponse> buscarRango(LocalDateTime desde, LocalDateTime hasta);
}
