package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Dados de um pagamento registrado no pedido")
public class PagamentoResponseDTO {

    @Schema(description = "Identificador do pagamento", example = "1")
    private Long id;

    @Schema(description = "Valor pago", example = "150.00")
    private BigDecimal valor;

    @Schema(description = "Nome do usuário que registrou o pagamento", example = "João da Silva")
    private String registradoPor;

    @Schema(description = "Data e hora do registro do pagamento", example = "2025-12-24T10:00:00")
    private LocalDateTime dataRegistro;

    @Schema(description = "Indica se o pagamento está válido e é contabilizado no total pago", example = "true")
    private boolean valido;

    @Schema(description = "Data e hora em que o pagamento foi invalidado", nullable = true)
    private LocalDateTime invaliadoEm;

    @Schema(description = "Nome do usuário que invalidou o pagamento", nullable = true)
    private String invaliadoPor;
}
