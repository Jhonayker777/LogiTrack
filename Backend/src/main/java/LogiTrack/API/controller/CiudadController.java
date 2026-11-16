package LogiTrack.API.controller;

import LogiTrack.API.dto.request.CiudadDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.service.CiudadService;
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

@Tag(name = "Ciudades", description = "CRUD de ciudades")
@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
public class CiudadController {

    private final CiudadService ciudadService;

    @Operation(summary = "Registrar ciudad", description = "Recibe los datos y crea la ciudad en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ciudad creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<CiudadDtoResponse> guardar(@Valid @RequestBody CiudadDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ciudadService.guardar(dto));
    }

    @Operation(summary = "Listar ciudades", description = "Devuelve todos los registros de ciudades")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<CiudadDtoResponse>> listar() {
        return ResponseEntity.ok(ciudadService.listar());
    }

    @Operation(summary = "Buscar ciudad por ID", description = "Busca la ciudad por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ciudad encontrada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CiudadDtoResponse> buscar(@Parameter(description = "ID de la ciudad") @PathVariable Long id) {
        return ResponseEntity.ok(ciudadService.buscarId(id));
    }

    @Operation(summary = "Actualizar ciudad", description = "Actualiza los datos de la ciudad indicada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ciudad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CiudadDtoResponse> actualizar(@Valid @RequestBody CiudadDtoRequest dto, @Parameter(description = "ID de la ciudad") @PathVariable Long id) {
        return ResponseEntity.ok(ciudadService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar ciudad", description = "Elimina la ciudad si no está referenciada por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ciudad eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID de la ciudad") @PathVariable Long id) {
        ciudadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar ciudades de un departamento", description = "Filtra las ciudades por el ID de su departamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/departamento/{id}")
    public ResponseEntity<List<CiudadDtoResponse>> buscarDepartamento(@Parameter(description = "ID del departamento") @PathVariable Long id) {
        return ResponseEntity.ok(ciudadService.buscarDepartamentoId(id));
    }
}
