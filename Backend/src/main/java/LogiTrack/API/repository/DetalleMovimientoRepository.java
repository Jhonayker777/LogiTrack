package LogiTrack.API.repository;

import LogiTrack.API.Model.DetalleMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {

    // Para listar los productos de un movimiento específico
    List<DetalleMovimiento> findByMovimientoId(Long movimientoId);

    // Para saber en qué movimientos ha estado un producto
    List<DetalleMovimiento> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);
}
