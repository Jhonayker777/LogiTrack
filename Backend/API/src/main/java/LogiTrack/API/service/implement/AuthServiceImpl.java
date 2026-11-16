package LogiTrack.API.service.implement;

import LogiTrack.API.auth.LoginRequest;
import LogiTrack.API.auth.LoginResponse;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.security.JwtService;
import LogiTrack.API.service.AuthService;
import LogiTrack.API.service.SolicitudRegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SolicitudRegistroService solicitudRegistroService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Mismo mensaje para usuario inexistente, inactivo o contraseña incorrecta (no revela cuál falló)
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String rol = usuario.getRol().name();
        String token = jwtService.generateToken(usuario.getUsername(), rol);
        return new LoginResponse(token, "Bearer", usuario.getUsername(), rol, jwtService.getExpirationMs() / 1000);
    }

    @Override
    public SolicitudRegistroDtoResponse register(SolicitudRegistroDtoRequest dto) {
        return solicitudRegistroService.guardar(dto);
    }
}
