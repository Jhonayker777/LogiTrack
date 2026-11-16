package LogiTrack.API.controller;

import LogiTrack.API.dto.response.AuditoriaDtoResponse;
import LogiTrack.API.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auditoría", description = "Consulta de cambios registrados ")
@RestController
@RequestMapping("/api/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @Operation(summary = "Listar auditorías", description = "Devuelve todos los registros de auditoría, del más reciente al más antiguo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<AuditoriaDtoResponse>> listar() {
        return ResponseEntity.ok(auditoriaService.listar());
    }

    @Operation(summary = "Buscar auditoría por ID", description = "Busca un registro de auditoría por su identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuditoriaDtoResponse> buscar(@Parameter(description = "ID de la auditoría") @PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarId(id));
    }

    @Operation(summary = "Auditorías por entidad", description = "Filtra por entidad afectada. Ej: bodega, producto, usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/entidad/{entidad}")
    public ResponseEntity<List<AuditoriaDtoResponse>> buscarEntidad(@Parameter(description = "Nombre de la entidad") @PathVariable String entidad) {
        return ResponseEntity.ok(auditoriaService.buscarEntidad(entidad));
    }

    @Operation(summary = "Historial de un registro", description = "Todos los cambios hechos a un registro concreto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/entidad/{entidad}/registro/{id}")
    public ResponseEntity<List<AuditoriaDtoResponse>> buscarRegistro(@PathVariable String entidad, @Parameter(description = "ID del registro afectado") @PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarRegistro(entidad, id));
    }

    @Operation(summary = "Auditorías por usuario", description = "Acciones realizadas por un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<AuditoriaDtoResponse>> buscarUsuario(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.buscarUsuarioId(id));
    }

    @Operation(summary = "Auditorías por rango de fechas", description = "Formato ISO: /api/auditorias/rango?desde=2026-11-01T00:00:00&hasta=2026-11-30T23:59:59")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @GetMapping("/rango")
    public ResponseEntity<List<AuditoriaDtoResponse>> buscarRango(@Parameter(description = "Fecha inicial (ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde, @Parameter(description = "Fecha final (ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(auditoriaService.buscarRango(desde, hasta));
    }
}
