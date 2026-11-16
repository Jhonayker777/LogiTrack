package LogiTrack.API.repository;

import LogiTrack.API.Model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad, Long> {

    List<Ciudad> findByDepartamentoId(Long departamentoId);

    Optional<Ciudad> findByNombreAndDepartamentoId(String nombre, Long departamentoId);

    boolean existsByNombreAndDepartamentoId(String nombre, Long departamentoId);

    boolean existsByDepartamentoId(Long departamentoId);
}
