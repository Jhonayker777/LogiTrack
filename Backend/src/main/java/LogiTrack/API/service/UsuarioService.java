package LogiTrack.API.service;

import LogiTrack.API.dto.request.UsuarioDtoRequest;
import LogiTrack.API.dto.response.UsuarioDtoResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioDtoResponse guardar(UsuarioDtoRequest dto);

    UsuarioDtoResponse actualizar(UsuarioDtoRequest dto, Long id);

    void eliminar(Long id);

    List<UsuarioDtoResponse> listar();

    UsuarioDtoResponse buscarId(Long id);
}