package LogiTrack.API.dto.response;

import java.math.BigDecimal;

public record ProductoDtoResponse(
        Long id,
        String nombre,
        String categoria,
        BigDecimal precio,
        Integer stockMinimo,
        Boolean activo
) {
}