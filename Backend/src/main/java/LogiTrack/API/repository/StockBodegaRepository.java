package LogiTrack.API.repository;

import LogiTrack.API.Model.StockBodega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockBodegaRepository extends JpaRepository<StockBodega, Long> {

    Optional<StockBodega> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    boolean existsByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    boolean existsByBodegaId(Long bodegaId);

    List<StockBodega> findByBodegaId(Long bodegaId);
    List<StockBodega> findByProductoId(Long productoId);

    @Query("SELECT COALESCE(SUM(sb.cantidad), 0) FROM StockBodega sb WHERE sb.bodega.id = :bodegaId")
    Integer totalUnidadesEnBodega(@Param("bodegaId") Long bodegaId);

    @Query("SELECT sb FROM StockBodega sb JOIN FETCH sb.producto JOIN FETCH sb.bodega WHERE sb.bodega.id = :bodegaId")
    List<StockBodega> findByBodegaIdWithRelations(@Param("bodegaId") Long bodegaId);

    @Query("SELECT sb.bodega.nombre, COALESCE(SUM(sb.cantidad), 0) FROM StockBodega sb GROUP BY sb.bodega.id, sb.bodega.nombre")
    List<Object[]> stockTotalPorBodega();
}