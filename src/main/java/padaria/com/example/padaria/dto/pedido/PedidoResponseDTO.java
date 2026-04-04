package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "Dados do pedido retornados pela API")
public class PedidoResponseDTO {

    @Schema(description = "Identificador único do pedido", example = "1")
    private Long id;

    @Schema(description = "Nome do cliente", example = "Maria Oliveira")
    private String cliente;

    @Schema(description = "Telefone do cliente", example = "(11) 99999-8888")
    private String telefone;

    @Schema(description = "Data e hora de entrega", example = "2025-12-24T10:00:00")
    private LocalDateTime dataHoraEntrega;

    @Schema(description = "Descrição detalhada do pedido")
    private String descricaoPedido;

    @Schema(description = "Observações adicionais", nullable = true)
    private String observacao;

    @Schema(description = "Status atual do pedido", example = "PENDENTE")
    private StatusPedido statusPedido;

    @Schema(description = "Valor total do pedido", example = "350.00")
    private BigDecimal valorPedido;

    @Schema(description = "Indica se o pagamento foi realizado integralmente", example = "false")
    private boolean pagamentoIntegral;

    @Schema(description = "Valor do adiantamento pago pelo cliente", example = "150.00", nullable = true)
    private BigDecimal valorAdiantamento;

    @Schema(description = "Data em que o pedido foi cancelado", nullable = true)
    private LocalDate dataCancelamento;

    @Schema(description = "Motivo do cancelamento", nullable = true)
    private MotivoCancelamento motivoCancelamento;

    @Schema(description = "Observação do cancelamento (preenchida quando motivo for OUTRO)", nullable = true)
    private String obsCancelamento;

    @Schema(description = "Nome do usuário que cadastrou o pedido", example = "João da Silva")
    private String cadastradoPor;

    @Schema(description = "Data e hora da última alteração", example = "2025-04-03T14:00:00")
    private LocalDateTime dataUltimaAlteracao;

    @Schema(description = "Nome do usuário responsável pela última alteração", example = "João da Silva")
    private String alteradoPor;

    @Schema(description = "Indica se o pedido está atrasado: status PENDENTE com data de entrega no passado. Calculado, não armazenado no banco", example = "false")
    private boolean atrasado;

    public static PedidoResponseDTO de(Pedido pedido) {
        boolean atrasado = pedido.getStatusPedido() == StatusPedido.PENDENTE
                && pedido.getDataHoraEntrega().isBefore(LocalDateTime.now());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getCliente(),
                pedido.getTelefone(),
                pedido.getDataHoraEntrega(),
                pedido.getDescricaoPedido(),
                pedido.getObservacao(),
                pedido.getStatusPedido(),
                pedido.getValorPedido(),
                pedido.isPagamentoIntegral(),
                pedido.getValorAdiantamento(),
                pedido.getDataCancelamento(),
                pedido.getMotivoCancelamento(),
                pedido.getObsCancelamento(),
                pedido.getCadastradoPor().getNome(),
                pedido.getDataUltimaAlteracao(),
                pedido.getAlteradoPor().getNome(),
                atrasado
        );
    }
}
