package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.SolicitudRegistro;
import org.springframework.stereotype.Component;

@Component
public class SolicitudRegistroMapper {

    public SolicitudRegistroDtoResponse entityToDto(SolicitudRegistro s,
                                                    CiudadDtoResponse ciudadDto,
                                                    UsuarioResumenDtoResponse resueltoPor,
                                                    UsuarioResumenDtoResponse usuarioCreado) {
        if (s == null) return null;
        return new SolicitudRegistroDtoResponse(
                s.getId(),
                s.getNombre(),
                s.getApellido(),
                s.getDocumento(),
                s.getEmail(),
                s.getTelefono(),
                s.getDireccion(),
                ciudadDto,
                s.getUsername(),
                s.getRolSolicitado().name(),
                s.getTurno(),
                s.getFechaIngreso(),
                s.getEstado().name(),
                s.getMotivoRechazo(),
                s.getFechaSolicitud(),
                s.getFechaResolucion(),
                resueltoPor,
                usuarioCreado
        );
    }

    public SolicitudRegistro DtoToEntity(SolicitudRegistroDtoRequest dto, Ciudad ciudad, String passwordHasheado) {
        if (dto == null) return null;
        SolicitudRegistro s = new SolicitudRegistro();
        s.setNombre(dto.nombre());
        s.setApellido(dto.apellido());
        s.setDocumento(dto.documento());
        s.setEmail(dto.email());
        s.setTelefono(dto.telefono());
        s.setDireccion(dto.direccion());
        s.setCiudad(ciudad);
        s.setUsername(dto.username());
        s.setPassword(passwordHasheado);
        s.setRolSolicitado(dto.rolSolicitado());
        s.setTurno(dto.turno());
        s.setFechaIngreso(dto.fechaIngreso());
        s.setEstado(SolicitudRegistro.Estado.PENDIENTE);
        return s;
    }

    public void updateEntityFromDto(SolicitudRegistro s, SolicitudRegistroDtoRequest dto, Ciudad ciudad) {
        if (s == null || dto == null) return;
        s.setNombre(dto.nombre());
        s.setApellido(dto.apellido());
        s.setDocumento(dto.documento());
        s.setEmail(dto.email());
        s.setTelefono(dto.telefono());
        s.setDireccion(dto.direccion());
        s.setCiudad(ciudad);
        s.setRolSolicitado(dto.rolSolicitado());
        s.setTurno(dto.turno());
        s.setFechaIngreso(dto.fechaIngreso());
    }
}