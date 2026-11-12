package LogiTrack.API.Model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Operacion operacion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "valores_anteriores", columnDefinition = "JSON")
    private String valoresAnteriores;

    @Column(name = "valores_nuevos", columnDefinition = "JSON")
    private String valoresNuevos;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    public enum Operacion {
        INSERT, UPDATE, DELETE
    }
}