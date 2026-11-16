package LogiTrack.API.repository;

import LogiTrack.API.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByActivoTrue();

    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);

    // ── Requisito 6: productos con stock bajo (< stock_minimo) ──
    // Como el stock vive en stock_bodega, se calcula con SUM + subconsulta
    @Query("""
        SELECT p FROM Producto p
        WHERE (SELECT COALESCE(SUM(sb.cantidad), 0)
               FROM StockBodega sb WHERE sb.producto.id = p.id) < p.stockMinimo
    """)
    List<Producto> findProductosStockBajo();

    // ── Stock total de un producto ──
    @Query("SELECT COALESCE(SUM(sb.cantidad), 0) FROM StockBodega sb WHERE sb.producto.id = :productoId")
    Integer stockTotalByProducto(@Param("productoId") Long productoId);
}