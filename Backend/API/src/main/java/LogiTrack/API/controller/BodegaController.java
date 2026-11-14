package LogiTrack.API.controller;

import LogiTrack.API.dto.request.BodegaDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.service.BodegaService;
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

@Tag(name = "Bodegas", description = "CRUD de bodegas")
@RestController
@RequestMapping("/api/bodegas")
@RequiredArgsConstructor
public class BodegaController {

    private final BodegaService bodegaService;

    @Operation(summary = "Registrar bodega", description = "Recibe los datos y crea la bodega en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bodega creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<BodegaDtoResponse> guardar(@Valid @RequestBody BodegaDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bodegaService.guardar(dto));
    }

    @Operation(summary = "Listar bodegas", description = "Devuelve todos los registros de bodegas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<BodegaDtoResponse>> listar() {
        return ResponseEntity.ok(bodegaService.listar());
    }

    @Operation(summary = "Buscar bodega por ID", description = "Busca la bodega por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bodega encontrada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BodegaDtoResponse> buscar(@Parameter(description = "ID de la bodega") @PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.buscarId(id));
    }

    @Operation(summary = "Actualizar bodega", description = "Actualiza los datos de la bodega indicada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bodega actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BodegaDtoResponse> actualizar(@Valid @RequestBody BodegaDtoRequest dto, @Parameter(description = "ID de la bodega") @PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar bodega", description = "Elimina la bodega si no está referenciada por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bodega eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID de la bodega") @PathVariable Long id) {
        bodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar bodegas de una ciudad", description = "Filtra las bodegas por el ID de la ciudad donde están ubicadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/ciudad/{id}")
    public ResponseEntity<List<BodegaDtoResponse>> buscarCiudad(@Parameter(description = "ID de la ciudad") @PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.buscarCiudadId(id));
    }

    @Operation(summary = "Listar bodegas de un encargado", description = "Filtra las bodegas por el ID del usuario encargado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/encargado/{id}")
    public ResponseEntity<List<BodegaDtoResponse>> buscarEncargado(@Parameter(description = "ID del encargado") @PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.buscarEncargadoId(id));
    }
}
