package LogiTrack.API.auth;

public record LoginResponse(String token, String tipo, String username, String rol, long expiraEnSegundos) {
}
