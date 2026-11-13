package LogiTrack.API.dto.response;

import java.time.LocalDateTime;

public record AuditoriaDtoResponse(
        Long id,
        String operacion,
        LocalDateTime fechaHora,
        UsuarioResumenDtoResponse usuario,
        String entidadAfectada,
        Long registroId,
        String valoresAnteriores,
        String valoresNuevos,
        String ipOrigen,
        String userAgent
) {
}