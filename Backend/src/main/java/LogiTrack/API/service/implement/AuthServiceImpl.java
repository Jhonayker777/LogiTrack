package LogiTrack.API.service.implement;

import LogiTrack.API.auth.LoginRequest;
import LogiTrack.API.auth.LoginResponse;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.Model.Auditoria;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.AuditoriaRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.security.JwtService;
import LogiTrack.API.service.AuthService;
import LogiTrack.API.service.EmailService;
import LogiTrack.API.service.SolicitudRegistroService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SolicitudRegistroService solicitudRegistroService;
    private final AuditoriaRepository auditoriaRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String rol = usuario.getRol().name();
        String token = jwtService.generateToken(usuario.getUsername(), rol);

        // --- REGISTRAMOS LA AUDITORÍA DIRECTAMENTE CON EL REPOSITORIO ---
        Auditoria auditoria = Auditoria.builder()
                .operacion(Auditoria.Operacion.UPDATE)
                .fechaHora(LocalDateTime.now())
                .usuario(usuario) // Usamos el objeto que ya está en sesión
                .entidadAfectada("Login")
                .registroId(usuario.getId())
                .valoresNuevos("{\"mensaje\":\"Inicio de sesión exitoso\"}")
                .build();

        auditoriaRepository.save(auditoria);
        // ----------------------------------------------------------------

        return new LoginResponse(token, "Bearer", usuario.getUsername(), rol, jwtService.getExpirationMs() / 1000);
    }

    @Override
    public SolicitudRegistroDtoResponse register(SolicitudRegistroDtoRequest dto) {
        return solicitudRegistroService.guardar(dto);
    }
    @Override
    public void recuperarPassword(String email) { // Ya no devuelve String
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe usuario con ese email."));

        String passwordTemporal = "Logi" + (int)(Math.random() * 9000 + 1000);

        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuarioRepository.save(usuario);

        // Enviamos el correo real
        emailService.enviarPasswordTemporal(usuario.getEmail(), passwordTemporal);
    }
}