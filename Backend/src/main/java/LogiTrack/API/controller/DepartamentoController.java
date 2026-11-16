package LogiTrack.API.controller;

import LogiTrack.API.dto.request.DepartamentoDtoRequest;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.service.DepartamentoService;
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

@Tag(name = "Departamentos", description = "CRUD de departamentos (región geográfica)")
@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @Operation(summary = "Registrar departamento", description = "Recibe los datos y crea el departamento en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Departamento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<DepartamentoDtoResponse> guardar(@Valid @RequestBody DepartamentoDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoService.guardar(dto));
    }

    @Operation(summary = "Listar departamentos", description = "Devuelve todos los registros de departamentos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<DepartamentoDtoResponse>> listar() {
        return ResponseEntity.ok(departamentoService.listar());
    }

    @Operation(summary = "Buscar departamento por ID", description = "Busca el departamento por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoDtoResponse> buscar(@Parameter(description = "ID del departamento") @PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.buscarId(id));
    }

    @Operation(summary = "Actualizar departamento", description = "Actualiza los datos del departamento indicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoDtoResponse> actualizar(@Valid @RequestBody DepartamentoDtoRequest dto, @Parameter(description = "ID del departamento") @PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar departamento", description = "Elimina el departamento si no está referenciado por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Departamento eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del departamento") @PathVariable Long id) {
        departamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
