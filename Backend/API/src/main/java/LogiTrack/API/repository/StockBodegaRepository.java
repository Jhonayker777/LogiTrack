package LogiTrack.API.repository;

import LogiTrack.API.Model.StockBodega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockBodegaRepository extends JpaRepository<StockBodega, Long> {

    Optional<StockBodega> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    boolean existsByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    List<StockBodega> findByBodegaId(Long bodegaId);

    List<StockBodega> findByProductoId(Long productoId);

    boolean existsByBodegaId(Long bodegaId);

    boolean existsByProductoId(Long productoId);
}
