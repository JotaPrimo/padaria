package padaria.com.example.padaria.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.LastModifiedDate;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String cliente;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "data_hora_entrega", nullable = false)
    private LocalDateTime dataHoraEntrega;

    @Column(name = "descricao_pedido", nullable = false, columnDefinition = "TEXT")
    private String descricaoPedido;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pedido", nullable = false, length = 20)
    private StatusPedido statusPedido = StatusPedido.PENDENTE;

    @Column(name = "valor_pedido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPedido;

    @Column(name = "pagamento_integral", nullable = false)
    private boolean pagamentoIntegral;

    @Column(name = "valor_adiantamento", precision = 10, scale = 2)
    private BigDecimal valorAdiantamento;

    @Column(name = "data_cancelamento")
    private LocalDate dataCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_cancelamento", length = 30)
    private MotivoCancelamento motivoCancelamento;

    @Column(name = "obs_cancelamento", columnDefinition = "TEXT")
    private String obsCancelamento;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cadastrado_por", nullable = false, updatable = false)
    private Usuario cadastradoPor;

    @LastModifiedDate
    @Column(name = "data_ultima_alteracao")
    private LocalDateTime dataUltimaAlteracao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "alterado_por", nullable = false)
    private Usuario alteradoPor;


    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    private void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.dataUltimaAlteracao = LocalDateTime.now();
    }

    public boolean isPedidoCancelado() {
        return StatusPedido.CANCELADO.equals(statusPedido);
    }

    public boolean isPedidoPendente() {
        return StatusPedido.PENDENTE.equals(statusPedido);
    }

    public boolean isPedidoEntregue() {
        return StatusPedido.ENTREGUE.equals(statusPedido);
    }
}
