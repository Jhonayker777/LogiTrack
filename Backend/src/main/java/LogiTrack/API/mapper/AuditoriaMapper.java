package LogiTrack.API.mapper;

import LogiTrack.API.dto.response.AuditoriaDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.Model.Auditoria;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {

    public AuditoriaDtoResponse entityToDto(Auditoria auditoria, UsuarioResumenDtoResponse usuarioDto) {

        if(auditoria==null) return null;
        return new AuditoriaDtoResponse(auditoria.getId(), auditoria.getOperacion().name(),
                auditoria.getFechaHora(), usuarioDto, auditoria.getEntidadAfectada(),
                auditoria.getRegistroId(), filtrarPassword(auditoria.getValoresAnteriores()),
                filtrarPassword(auditoria.getValoresNuevos()), auditoria.getIpOrigen(),
                auditoria.getUserAgent());

    }

    private String filtrarPassword(String json) {

        if(json==null) return null;
        return json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");

    }

}