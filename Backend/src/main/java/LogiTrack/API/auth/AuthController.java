package LogiTrack.API.auth;

import LogiTrack.API.dto.request.RecuperarPasswordRequest;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Autenticación", description = "Login y registro (endpoints públicos)")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @SecurityRequirements // endpoint público: sin candado en Swagger
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve un token JWT con el rol del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto, devuelve el token"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @SecurityRequirements
    @Operation(summary = "Solicitar registro", description = "Crea una solicitud en estado PENDIENTE. Un ADMIN debe aprobarla (PUT /api/solicitudes/{id}/aprobar) antes de poder iniciar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o username/email ya registrados")
    })
    @PostMapping("/register")
    public ResponseEntity<SolicitudRegistroDtoResponse> register(@Valid @RequestBody SolicitudRegistroDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, String>> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        authService.recuperarPassword(request.email());
        return ResponseEntity.ok(Map.of("mensaje", "Si el correo existe, recibirás una contraseña temporal."));
    }
}
