package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.CiudadDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Departamento;
import org.springframework.stereotype.Component;

@Component
public class CiudadMapper {

    public CiudadDtoResponse entityToDto(Ciudad ciudad, DepartamentoDtoResponse departamentoDto) {
        if (ciudad == null) return null;
        return new CiudadDtoResponse(
                ciudad.getId(),
                ciudad.getNombre(),
                ciudad.getCodigoPostal(),
                departamentoDto
        );
    }

    public Ciudad DtoToEntity(CiudadDtoRequest dto, Departamento departamento) {
        if (dto == null || departamento == null) return null;
        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(dto.nombre());
        ciudad.setCodigoPostal(dto.codigoPostal());
        ciudad.setDepartamento(departamento);
        return ciudad;
    }

    public void updateEntityFromDto(Ciudad ciudad, CiudadDtoRequest dto, Departamento departamento) {
        if (ciudad == null || dto == null || departamento == null) return;
        ciudad.setNombre(dto.nombre());
        ciudad.setCodigoPostal(dto.codigoPostal());
        ciudad.setDepartamento(departamento);
    }
}