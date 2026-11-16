package LogiTrack.API.controller;

import LogiTrack.API.dto.request.StockBodegaDtoRequest;
import LogiTrack.API.dto.response.StockBodegaDtoResponse;
import LogiTrack.API.service.StockBodegaService;
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

@Tag(name = "Stock por bodega", description = "Inventario de cada producto en cada bodega")
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockBodegaController {

    private final StockBodegaService stockBodegaService;

    @Operation(summary = "Registrar registro de stock", description = "Recibe los datos y crea el registro de stock en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro de stock creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<StockBodegaDtoResponse> guardar(@Valid @RequestBody StockBodegaDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockBodegaService.guardar(dto));
    }

    @Operation(summary = "Listar registros de stock", description = "Devuelve todos los registros de registros de stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<StockBodegaDtoResponse>> listar() {
        return ResponseEntity.ok(stockBodegaService.listar());
    }

    @Operation(summary = "Buscar registro de stock por ID", description = "Busca el registro de stock por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro de stock encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StockBodegaDtoResponse> buscar(@Parameter(description = "ID del registro de stock") @PathVariable Long id) {
        return ResponseEntity.ok(stockBodegaService.buscarId(id));
    }

    @Operation(summary = "Actualizar registro de stock", description = "Actualiza los datos del registro de stock indicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro de stock actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StockBodegaDtoResponse> actualizar(@Valid @RequestBody StockBodegaDtoRequest dto, @Parameter(description = "ID del registro de stock") @PathVariable Long id) {
        return ResponseEntity.ok(stockBodegaService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar registro de stock", description = "Elimina el registro de stock si no está referenciado por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro de stock eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del registro de stock") @PathVariable Long id) {
        stockBodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Stock de una bodega", description = "Lista los productos y cantidades almacenados en una bodega")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/bodega/{id}")
    public ResponseEntity<List<StockBodegaDtoResponse>> buscarBodega(@Parameter(description = "ID de la bodega") @PathVariable Long id) {
        return ResponseEntity.ok(stockBodegaService.buscarBodegaId(id));
    }

    @Operation(summary = "Stock de un producto por bodega", description = "Lista en qué bodegas está un producto y en qué cantidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<StockBodegaDtoResponse>> buscarProducto(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(stockBodegaService.buscarProductoId(id));
    }

    @Operation(summary = "Stock de un producto en una bodega", description = "Consulta la cantidad exacta de un producto en una bodega")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/bodega/{bodegaId}/producto/{productoId}")
    public ResponseEntity<StockBodegaDtoResponse> buscarBodegaProducto(@PathVariable Long bodegaId, @PathVariable Long productoId) {
        return ResponseEntity.ok(stockBodegaService.buscarBodegaProducto(bodegaId, productoId));
    }

    @Operation(summary = "Stock total de un producto", description = "Suma la cantidad del producto en todas las bodegas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/producto/{id}/total")
    public ResponseEntity<Integer> stockTotal(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(stockBodegaService.stockTotalProducto(id));
    }
}
