package LogiTrack.API.audit;

import LogiTrack.API.Model.Auditoria;
import LogiTrack.API.Model.Usuario;
import LogiTrack.API.repository.AuditoriaRepository;
import LogiTrack.API.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaListener {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public AuditoriaListener(
            @Lazy AuditoriaRepository auditoriaRepository,
            @Lazy UsuarioRepository usuarioRepository,
            @Lazy ObjectMapper objectMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @PrePersist
    public void prePersist(Object entity) {
        registrar(entity, Auditoria.Operacion.INSERT, null, entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        registrar(entity, Auditoria.Operacion.UPDATE, null, entity);
    }

    @PreRemove
    public void preRemove(Object entity) {
        registrar(entity, Auditoria.Operacion.DELETE, entity, null);
    }

    private void registrar(Object entity, Auditoria.Operacion operacion, Object valoresAntiguos, Object valoresNuevos) {
        // Evitamos auditar la propia tabla de auditoría para no crear un bucle infinito
        if (entity instanceof Auditoria) return;

        try {
            String nombreEntidad = entity.getClass().getSimpleName();
            Long registroId = obtenerId(entity);
            String username = obtenerUsuarioActual();

            Usuario usuario = (username != null) ? usuarioRepository.findByUsername(username).orElse(null) : null;

            String jsonAntiguos = (valoresAntiguos != null) ? objectMapper.writeValueAsString(valoresAntiguos) : null;
            String jsonNuevos = (valoresNuevos != null) ? objectMapper.writeValueAsString(valoresNuevos) : null;

            // Enmascaramos el password si la entidad es un Usuario
            if ("Usuario".equals(nombreEntidad)) {
                jsonAntiguos = enmascararPassword(jsonAntiguos);
                jsonNuevos = enmascararPassword(jsonNuevos);
            }

            Auditoria auditoria = Auditoria.builder()
                    .operacion(operacion)
                    .fechaHora(java.time.LocalDateTime.now())
                    .usuario(usuario)
                    .entidadAfectada(nombreEntidad)
                    .registroId(registroId)
                    .valoresAnteriores(jsonAntiguos)
                    .valoresNuevos(jsonNuevos)
                    .build();

            auditoriaRepository.save(auditoria);

        } catch (Exception e) {
            System.err.println("Error al guardar auditoría: " + e.getMessage());
        }
    }

    private Long obtenerId(Object entity) {
        try {
            // Asumimos que todas las entidades tienen un método getId()
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "sistema";
    }

    private String enmascararPassword(String json) {
        if (json == null) return null;
        return json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
    }
}