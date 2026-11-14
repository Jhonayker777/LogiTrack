package LogiTrack.API.controller;

import LogiTrack.API.dto.request.DetalleMovimientoDtoRequest;
import LogiTrack.API.dto.response.DetalleMovimientoDtoResponse;
import LogiTrack.API.service.DetalleMovimientoService;
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

@Tag(name = "Detalles de movimiento", description = "Productos y cantidades de cada movimiento")
@RestController
@RequestMapping("/api/detalles-movimiento")
@RequiredArgsConstructor
public class DetalleMovimientoController {

    private final DetalleMovimientoService detalleMovimientoService;

    @Operation(summary = "Registrar detalle de movimiento", description = "Recibe los datos y crea el detalle de movimiento en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Detalle de movimiento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<DetalleMovimientoDtoResponse> guardar(@Valid @RequestBody DetalleMovimientoDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detalleMovimientoService.guardar(dto));
    }

    @Operation(summary = "Listar detalles de movimiento", description = "Devuelve todos los registros de detalles de movimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<DetalleMovimientoDtoResponse>> listar() {
        return ResponseEntity.ok(detalleMovimientoService.listar());
    }

    @Operation(summary = "Buscar detalle de movimiento por ID", description = "Busca el detalle de movimiento por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DetalleMovimientoDtoResponse> buscar(@Parameter(description = "ID del detalle de movimiento") @PathVariable Long id) {
        return ResponseEntity.ok(detalleMovimientoService.buscarId(id));
    }

    @Operation(summary = "Actualizar detalle de movimiento", description = "Actualiza los datos del detalle de movimiento indicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de movimiento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DetalleMovimientoDtoResponse> actualizar(@Valid @RequestBody DetalleMovimientoDtoRequest dto, @Parameter(description = "ID del detalle de movimiento") @PathVariable Long id) {
        return ResponseEntity.ok(detalleMovimientoService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar detalle de movimiento", description = "Elimina el detalle de movimiento si no está referenciado por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Detalle de movimiento eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del detalle de movimiento") @PathVariable Long id) {
        detalleMovimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Detalles de un movimiento", description = "Lista los productos y cantidades de un movimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/movimiento/{id}")
    public ResponseEntity<List<DetalleMovimientoDtoResponse>> buscarMovimiento(@Parameter(description = "ID del movimiento") @PathVariable Long id) {
        return ResponseEntity.ok(detalleMovimientoService.buscarMovimientoId(id));
    }

    @Operation(summary = "Movimientos de un producto", description = "Lista los detalles de movimiento en los que participa un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<DetalleMovimientoDtoResponse>> buscarProducto(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(detalleMovimientoService.buscarProductoId(id));
    }
}
