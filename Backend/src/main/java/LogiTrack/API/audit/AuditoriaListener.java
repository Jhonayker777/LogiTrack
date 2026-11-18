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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;

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
        // En un UPDATE, lo ideal sería capturar los valores anteriores,
        // pero JPA no los provee directamente en el listener.
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

            Map<String, Object> jsonAntiguos = (valoresAntiguos != null) ? extraerCamposSimples(valoresAntiguos) : null;
            Map<String, Object> jsonNuevos = (valoresNuevos != null) ? extraerCamposSimples(valoresNuevos) : null;

            // Enmascaramos el password si la entidad es un Usuario
            if ("Usuario".equals(nombreEntidad)) {
                if (jsonAntiguos != null && jsonAntiguos.containsKey("password")) {
                    jsonAntiguos.put("password", "***");
                }
                if (jsonNuevos != null && jsonNuevos.containsKey("password")) {
                    jsonNuevos.put("password", "***");
                }
            }

            Auditoria auditoria = Auditoria.builder()
                    .operacion(operacion)
                    .fechaHora(LocalDateTime.now())
                    .usuario(usuario)
                    .entidadAfectada(nombreEntidad)
                    .registroId(registroId)
                    .valoresAnteriores(objectMapper.writeValueAsString(jsonAntiguos))
                    .valoresNuevos(objectMapper.writeValueAsString(jsonNuevos))
                    .build();

            auditoriaRepository.save(auditoria);

        } catch (Exception e) {
            System.err.println("Error al guardar auditoría: " + e.getMessage());
        }
    }

    private Map<String, Object> extraerCamposSimples(Object obj) {
        Map<String, Object> mapa = new HashMap<>();
        Class<?> clazz = obj.getClass();

        // Recorremos la clase y sus superclases (por si hay herencia, aunque en tu caso no aplica)
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                // Ignoramos campos estáticos o sintéticos (como los proxies de Hibernate)
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                field.setAccessible(true);
                try {
                    Object valor = field.get(obj);

                    if (valor == null || valor instanceof String || valor instanceof Number ||
                            valor instanceof Boolean || valor instanceof Temporal || valor instanceof Enum) {
                        mapa.put(field.getName(), valor);
                    }
                } catch (IllegalAccessException e) {
                }
            }
            clazz = clazz.getSuperclass();
        }
        return mapa;
    }

    private Long obtenerId(Object entity) {
        try {
            // Buscamos el método getId() en la clase
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
        return "sistema"; // Para acciones internas o pruebas desde la BD
    }
}