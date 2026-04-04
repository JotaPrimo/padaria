package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Indicadores do painel de controle da padaria")
public class DashboardResponseDTO {

    @Schema(description = "Total de pedidos com entrega agendada para o dia atual", example = "3")
    private long pedidosHoje;

    @Schema(description = "Total de pedidos com entrega agendada para a semana corrente", example = "12")
    private long pedidosSemana;

    @Schema(description = "Total de pedidos com entrega agendada para o mês corrente", example = "45")
    private long pedidosMes;

    @Schema(description = "Total de pedidos com pagamento pendente (pagamento não integral, exceto cancelados)", example = "7")
    private long pedidosPagamentoPendente;
}
