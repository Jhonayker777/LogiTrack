package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.BodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class BodegaMapper {

    public BodegaDtoResponse entityToDto(Bodega b, UsuarioResumenDtoResponse encargadoDto, CiudadDtoResponse ciudadDto) {
        if (b == null) return null;
        return new BodegaDtoResponse(
                b.getId(),
                b.getNombre(),
                b.getUbicacion(),
                b.getCapacidad(),
                encargadoDto,
                ciudadDto,
                b.getActivo()
        );
    }

    public Bodega DtoToEntity(BodegaDtoRequest dto, Usuario encargado, Ciudad ciudad) {
        if (dto == null) return null;
        Bodega b = new Bodega();
        b.setNombre(dto.nombre());
        b.setUbicacion(dto.ubicacion());
        b.setCapacidad(dto.capacidad());
        b.setEncargado(encargado);
        b.setCiudad(ciudad);
        b.setActivo(true);
        return b;
    }

    public void updateEntityFromDto(Bodega b, BodegaDtoRequest dto, Usuario encargado, Ciudad ciudad) {
        if (b == null || dto == null) return;
        b.setNombre(dto.nombre());
        b.setUbicacion(dto.ubicacion());
        b.setCapacidad(dto.capacidad());
        b.setEncargado(encargado);
        b.setCiudad(ciudad);
    }
}