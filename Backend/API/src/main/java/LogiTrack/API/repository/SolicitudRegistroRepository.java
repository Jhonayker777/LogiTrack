package LogiTrack.API.repository;

import LogiTrack.API.Model.SolicitudRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitudRegistroRepository extends JpaRepository<SolicitudRegistro, Long> {

    List<SolicitudRegistro> findByEstado(SolicitudRegistro.Estado estado);

    List<SolicitudRegistro> findByEstadoOrderByFechaSolicitudAsc(SolicitudRegistro.Estado estado);

    Optional<SolicitudRegistro> findByUsername(String username);

    Optional<SolicitudRegistro> findByEmail(String email);

    Optional<SolicitudRegistro> findByDocumento(String documento);

    boolean existsByUsernameAndEstado(String username, SolicitudRegistro.Estado estado);

    boolean existsByEmailAndEstado(String email, SolicitudRegistro.Estado estado);

    boolean existsByDocumentoAndEstado(String documento, SolicitudRegistro.Estado estado);

    List<SolicitudRegistro> findByFechaSolicitudBetween(LocalDateTime desde, LocalDateTime hasta);
}