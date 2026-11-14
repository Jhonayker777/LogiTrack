package LogiTrack.API.repository;

import LogiTrack.API.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDocumento(String documento);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByDocumento(String documento);

    List<Usuario> findByRol(Usuario.Rol rol);

    List<Usuario> findByActivoTrue();

    // Usuarios por ciudad
    @Query("SELECT u FROM Usuario u JOIN FETCH u.ciudad WHERE u.ciudad.id = :ciudadId")
    List<Usuario> findByCiudadIdWithCiudad(@Param("ciudadId") Long ciudadId);
}