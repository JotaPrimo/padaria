package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import padaria.com.example.padaria.enums.StatusPedido;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Parâmetros de filtragem para a listagem de pedidos")
public class PedidoFiltroDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Início do intervalo de data de entrega (inclusive)", example = "2025-12-01T00:00:00")
    private LocalDateTime dataEntregaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Fim do intervalo de data de entrega (inclusive)", example = "2025-12-31T23:59:59")
    private LocalDateTime dataEntregaFim;

    @Schema(description = "Busca parcial pelo nome do cliente (sem distinção de maiúsculas)", example = "Maria")
    private String cliente;

    @Schema(description = "Filtrar pelo status do pedido", example = "PENDENTE")
    private StatusPedido statusPedido;

    @Schema(description = "Filtrar apenas pedidos com pagamento pendente", example = "true")
    private Boolean pagamentoPendente;

    @Schema(description = "Filtrar pelo ID do usuário que cadastrou o pedido", example = "2")
    private Long cadastradoPorId;
}
