package LogiTrack.API.service;

import LogiTrack.API.dto.request.CiudadDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;

import java.util.List;

public interface CiudadService {
    CiudadDtoResponse guardar(CiudadDtoRequest dto);

    CiudadDtoResponse actualizar(CiudadDtoRequest dto, Long id);

    void eliminar(Long id);

    List<CiudadDtoResponse> listar();

    CiudadDtoResponse buscarId(Long id);

    List<CiudadDtoResponse> buscarDepartamentoId(Long id);
}
