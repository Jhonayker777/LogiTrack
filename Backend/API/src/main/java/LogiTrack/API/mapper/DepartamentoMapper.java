package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.DepartamentoDtoRequest;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.Model.Departamento;
import org.springframework.stereotype.Component;

@Component
public class DepartamentoMapper {

    public DepartamentoDtoResponse entityToDto(Departamento departamento) {
        if (departamento == null) return null;
        return new DepartamentoDtoResponse(
                departamento.getId(),
                departamento.getNombre(),
                departamento.getPais()
        );
    }

    public Departamento DtoToEntity(DepartamentoDtoRequest dto) {
        if (dto == null) return null;
        Departamento departamento = new Departamento();
        departamento.setNombre(dto.nombre());
        departamento.setPais(dto.pais() != null ? dto.pais() : "Colombia");
        return departamento;
    }

    public void updateEntityFromDto(Departamento departamento, DepartamentoDtoRequest dto) {
        if (departamento == null || dto == null) return;
        departamento.setNombre(dto.nombre());
        if (dto.pais() != null) {
            departamento.setPais(dto.pais());
        }
    }
}