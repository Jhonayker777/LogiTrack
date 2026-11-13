package LogiTrack.API.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MovimientoDtoResponse(
        Long id,
        String tipo,
        LocalDateTime fecha,
        UsuarioResumenDtoResponse usuario,
        BodegaDtoResponse bodegaOrigen,
        BodegaDtoResponse bodegaDestino,
        String observacion
) {
}