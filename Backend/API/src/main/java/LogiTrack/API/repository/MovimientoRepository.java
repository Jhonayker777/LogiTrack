package LogiTrack.API.repository;

import LogiTrack.API.Model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    List<Movimiento> findByTipo(Movimiento.Tipo tipo);

    List<Movimiento> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByBodegaOrigenIdOrBodegaDestinoId(Long origenId, Long destinoId);
}
