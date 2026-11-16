package LogiTrack.API.controller;

import LogiTrack.API.dto.request.RechazoDtoRequest;
import LogiTrack.API.dto.request.SolicitudRegistroDtoRequest;
import LogiTrack.API.dto.response.SolicitudRegistroDtoResponse;
import LogiTrack.API.service.SolicitudRegistroService;
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

@Tag(name = "Solicitudes de registro", description = "Flujo de alta de usuarios con aprobación del administrador")
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudRegistroController {

    private final SolicitudRegistroService solicitudRegistroService;

    @Operation(summary = "Solicitar registro", description = "Un aspirante envía sus datos. Queda en estado PENDIENTE hasta que un administrador la apruebe o rechace")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<SolicitudRegistroDtoResponse> guardar(@Valid @RequestBody SolicitudRegistroDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudRegistroService.guardar(dto));
    }

    @Operation(summary = "Listar solicitudes", description = "Devuelve todas las solicitudes de registro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<SolicitudRegistroDtoResponse>> listar() {
        return ResponseEntity.ok(solicitudRegistroService.listar());
    }

    @Operation(summary = "Listar solicitudes por estado", description = "Estados válidos: PENDIENTE, APROBADA, RECHAZADA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudRegistroDtoResponse>> buscarEstado(@Parameter(description = "PENDIENTE, APROBADA o RECHAZADA") @PathVariable String estado) {
        return ResponseEntity.ok(solicitudRegistroService.buscarEstado(estado));
    }

    @Operation(summary = "Buscar solicitud por ID", description = "Busca una solicitud por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudRegistroDtoResponse> buscar(@Parameter(description = "ID de la solicitud") @PathVariable Long id) {
        return ResponseEntity.ok(solicitudRegistroService.buscarId(id));
    }

    @Operation(summary = "Aprobar solicitud", description = "Crea el usuario real a partir de la solicitud. El administrador se toma del token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aprobada y usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudRegistroDtoResponse> aprobar(@Parameter(description = "ID de la solicitud") @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(solicitudRegistroService.aprobar(id, principal.getName()));
    }

    @Operation(summary = "Rechazar solicitud", description = "Rechaza la solicitud indicando el motivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudRegistroDtoResponse> rechazar(@Parameter(description = "ID de la solicitud") @PathVariable Long id, @Valid @RequestBody RechazoDtoRequest dto, Principal principal) {
        return ResponseEntity.ok(solicitudRegistroService.rechazar(id, dto, principal.getName()));
    }
}
