package LogiTrack.API.repository;

import LogiTrack.API.Model.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    Optional<Bodega> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Bodega> findByActivoTrue();

    List<Bodega> findByEncargadoId(Long encargadoId);

    List<Bodega> findByCiudadId(Long ciudadId);

    @Query("SELECT b FROM Bodega b JOIN FETCH b.encargado JOIN FETCH b.ciudad WHERE b.id = :id")
    Optional<Bodega> findByIdWithRelations(@Param("id") Long id);
}