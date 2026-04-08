package padaria.com.example.padaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, updatable = false)
    private Pedido pedido;

    @Column(nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false, updatable = false)
    private Usuario registradoPor;

    private Boolean  adiantamento = false;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @Column(nullable = false)
    private Boolean  valido = true;

    @Column(name = "invaliadoEm")
    private LocalDateTime invaliadoEm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invaliadoPor")
    private Usuario invaliadoPor;

    @PrePersist
    private void prePersist() {
        this.dataRegistro = LocalDateTime.now();
    }
}
