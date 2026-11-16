package LogiTrack.API.service;

import LogiTrack.API.dto.request.DepartamentoDtoRequest;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;

import java.util.List;

public interface DepartamentoService {
    DepartamentoDtoResponse guardar(DepartamentoDtoRequest dto);

    DepartamentoDtoResponse actualizar(DepartamentoDtoRequest dto, Long id);

    void eliminar(Long id);

    List<DepartamentoDtoResponse> listar();

    DepartamentoDtoResponse buscarId(Long id);
}
