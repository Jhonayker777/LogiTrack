package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.UsuarioDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.UsuarioDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDtoResponse entityToDto(Usuario usuario, CiudadDtoResponse ciudadDto) {
        if (usuario == null) return null;
        return new UsuarioDtoResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDocumento(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                ciudadDto,
                usuario.getUsername(),
                usuario.getRol().name(),
                usuario.getTurno(),
                usuario.getFechaIngreso(),
                usuario.getActivo()
        );
    }

    public UsuarioResumenDtoResponse entityToResumenDto(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioResumenDtoResponse(
                usuario.getId(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getUsername(),
                usuario.getRol().name()
        );
    }

    public Usuario DtoToEntity(UsuarioDtoRequest dto, Ciudad ciudad, String passwordHasheado) {
        if (dto == null) return null;
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setDocumento(dto.documento());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setCiudad(ciudad);
        usuario.setUsername(dto.username());
        usuario.setPassword(passwordHasheado);
        usuario.setRol(dto.rol());
        usuario.setTurno(dto.turno());
        usuario.setFechaIngreso(dto.fechaIngreso());
        usuario.setActivo(true);
        return usuario;
    }

    public void updateEntityFromDto(Usuario usuario, UsuarioDtoRequest dto, Ciudad ciudad) {
        if (usuario == null || dto == null) return;
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setDocumento(dto.documento());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setCiudad(ciudad);
        usuario.setUsername(dto.username());
        usuario.setRol(dto.rol());
        usuario.setTurno(dto.turno());
        usuario.setFechaIngreso(dto.fechaIngreso());
    }
}