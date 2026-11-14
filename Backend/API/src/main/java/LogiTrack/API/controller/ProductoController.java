package LogiTrack.API.controller;

import LogiTrack.API.dto.request.ProductoDtoRequest;
import LogiTrack.API.dto.response.ProductoDtoResponse;
import LogiTrack.API.service.ProductoService;
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

@Tag(name = "Productos", description = "CRUD de productos y consulta de stock bajo")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Registrar producto", description = "Recibe los datos y crea el producto en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada")
    })
    @PostMapping
    public ResponseEntity<ProductoDtoResponse> guardar(@Valid @RequestBody ProductoDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(dto));
    }

    @Operation(summary = "Listar productos", description = "Devuelve todos los registros de productos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<ProductoDtoResponse>> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    @Operation(summary = "Buscar producto por ID", description = "Busca el producto por su identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDtoResponse> buscar(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarId(id));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza los datos del producto indicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDtoResponse> actualizar(@Valid @RequestBody ProductoDtoRequest dto, @Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(productoService.actualizar(dto, id));
    }

    @Operation(summary = "Eliminar producto", description = "Elimina el producto si no está referenciado por otros registros")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio violada"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del producto") @PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar productos por categoría", description = "Filtra los productos de una categoría. Ej: /api/productos/categoria?nombre=Electronica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/categoria")
    public ResponseEntity<List<ProductoDtoResponse>> buscarCategoria(@Parameter(description = "Nombre de la categoría") @RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarCategoria(nombre));
    }

    @Operation(summary = "Productos con stock bajo", description = "Devuelve los productos cuyo stock total (suma de todas las bodegas) es menor a su stock mínimo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoDtoResponse>> stockBajo() {
        return ResponseEntity.ok(productoService.listarStockBajo());
    }
}
