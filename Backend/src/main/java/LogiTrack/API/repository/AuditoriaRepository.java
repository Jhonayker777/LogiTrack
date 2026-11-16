package LogiTrack.API.repository;

import LogiTrack.API.Model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();

    List<Auditoria> findByEntidadAfectadaOrderByFechaHoraDesc(String entidadAfectada);

    List<Auditoria> findByEntidadAfectadaAndRegistroIdOrderByFechaHoraDesc(String entidadAfectada, Long registroId);

    List<Auditoria> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    List<Auditoria> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);
}
