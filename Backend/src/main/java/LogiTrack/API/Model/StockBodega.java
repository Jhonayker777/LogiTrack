package LogiTrack.API.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_bodega")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBodega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 0;
}
