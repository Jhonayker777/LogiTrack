package LogiTrack.API.service;

import LogiTrack.API.dto.request.BodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;

import java.util.List;

public interface BodegaService {
    BodegaDtoResponse guardar(BodegaDtoRequest dto);

    BodegaDtoResponse actualizar(BodegaDtoRequest dto, Long id);

    void eliminar(Long id);

    List<BodegaDtoResponse> listar();

    BodegaDtoResponse buscarId(Long id);

    List<BodegaDtoResponse> buscarCiudadId(Long id);

    List<BodegaDtoResponse> buscarEncargadoId(Long id);
}
