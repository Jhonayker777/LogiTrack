
package LogiTrack.API.controller;

import LogiTrack.API.dto.request.UsuarioDtoRequest;
import LogiTrack.API.dto.request.PerfilUsuarioDtoRequest;
import LogiTrack.API.dto.response.UsuarioDtoResponse;
import LogiTrack.API.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Tag(name = "Usuarios",
        description = "Administración de usuarios del sistema")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Consultar perfil del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UsuarioDtoResponse> miPerfil(
            Authentication authentication) {

        return ResponseEntity.ok(
                usuarioService.buscarPorUsername(authentication.getName())
        );
    }

    // Actualizar perfil del usuario autenticado
    @PutMapping("/me")
    public ResponseEntity<UsuarioDtoResponse> actualizarMiPerfil(
            @Valid @RequestBody PerfilUsuarioDtoRequest dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                usuarioService.actualizarPerfil(
                        authentication.getName(), dto
                )
        );
    }

    @Operation(summary = "Registrar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400",
                    description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<UsuarioDtoResponse> guardar(
            @Valid @RequestBody UsuarioDtoRequest dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.guardar(dto));
    }

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public ResponseEntity<List<UsuarioDtoResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(summary = "Buscar usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDtoResponse> buscar(
            @Parameter(description = "ID del usuario")
            @PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.buscarId(id));
    }

    @Operation(summary = "Actualizar usuario")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDtoResponse> actualizar(
            @Valid @RequestBody UsuarioDtoRequest dto,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                usuarioService.actualizar(dto, id)
        );
    }

    @Operation(summary = "Eliminar usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        usuarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
