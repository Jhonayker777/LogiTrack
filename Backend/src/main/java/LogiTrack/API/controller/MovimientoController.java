package LogiTrack.API.controller;

import LogiTrack.API.dto.request.MovimientoDtoRequest;
import LogiTrack.API.dto.response.MovimientoDtoResponse;
import LogiTrack.API.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Movimientos", description = "Entradas, salidas y transferencias de inventario (historial inmutable)")
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @Operation(summary = "Registrar movimiento", description = "Registra una ENTRADA, SALIDA o TRANSFERENCIA. El usuario responsable se toma del token JWT y el stock se actualiza automáticamente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimiento registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<MovimientoDtoResponse> guardar(@Valid @RequestBody MovimientoDtoRequest dto, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.guardar(dto, principal.getName()));
    }

    @Operation(summary = "Listar movimientos", description = "Devuelve el historial completo de movimientos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<MovimientoDtoResponse>> listar() {
        return ResponseEntity.ok(movimientoService.listar());
    }

    @Operation(summary = "Buscar movimiento por ID", description = "Busca un movimiento por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoDtoResponse> buscar(@Parameter(description = "ID del movimiento") @PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.buscarId(id));
    }
}
