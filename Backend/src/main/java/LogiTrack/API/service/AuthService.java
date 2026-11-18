package LogiTrack.API.service;

import LogiTrack.API.auth.LoginRequest;
import LogiTrack.API.auth.LoginResponse;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    // El registro público crea una solicitud PENDIENTE que un ADMIN debe aprobar
    SolicitudRegistroDtoResponse register(SolicitudRegistroDtoRequest dto);

    void recuperarPassword(String email);
}
