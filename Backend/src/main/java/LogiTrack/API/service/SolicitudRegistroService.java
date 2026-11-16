package LogiTrack.API.service;

import LogiTrack.API.dto.request.RechazoDtoRequest;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;

import java.util.List;

public interface SolicitudRegistroService {

    // Para el aspirante
    SolicitudRegistroDtoResponse guardar(SolicitudRegistroDtoRequest dto);

    // Para el administrador
    SolicitudRegistroDtoResponse aprobar(Long id, String usernameAdmin);
    SolicitudRegistroDtoResponse rechazar(Long id, RechazoDtoRequest dto, String usernameAdmin);

    // Consultas
    List<SolicitudRegistroDtoResponse> listar();
    List<SolicitudRegistroDtoResponse> buscarEstado(String estado);
    SolicitudRegistroDtoResponse buscarId(Long id);
}