package LogiTrack.API.service;

import LogiTrack.API.dto.request.MovimientoDtoRequest;
import LogiTrack.API.dto.response.MovimientoDtoResponse;

import java.util.List;

public interface MovimientoService {
    // Los movimientos son inmutables (historial): no hay actualizar ni eliminar
    MovimientoDtoResponse guardar(MovimientoDtoRequest dto, String usernameUsuario);

    List<MovimientoDtoResponse> listar();

    MovimientoDtoResponse buscarId(Long id);
}