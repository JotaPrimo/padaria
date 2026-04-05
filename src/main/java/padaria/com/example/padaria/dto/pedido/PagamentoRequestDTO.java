package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "Dados para registro de um pagamento no pedido")
public class PagamentoRequestDTO {

    @NotNull(message = "O campo valor é obrigatório.")
    @Positive(message = "O valor do pagamento deve ser maior que zero.")
    @Schema(description = "Valor do pagamento a registrar", example = "150.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valor;
}
