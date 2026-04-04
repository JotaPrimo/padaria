package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import padaria.com.example.padaria.enums.MotivoCancelamento;

@Getter
@Schema(description = "Dados para cancelamento de um pedido")
public class PedidoCancelamentoDTO {

    @NotNull(message = "O campo motivo do cancelamento é obrigatório.")
    @Schema(
        description = "Motivo do cancelamento",
        example = "CANCELADO_PELO_CLIENTE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"CANCELADO_PELO_CLIENTE", "CANCELADO_PELA_PADARIA", "OUTRO"}
    )
    private MotivoCancelamento motivoCancelamento;

    @Schema(
        description = "Descrição livre do motivo. Obrigatória quando motivoCancelamento for OUTRO",
        example = "Cliente desistiu por problemas financeiros",
        nullable = true
    )
    private String obsCancelamento;

    @Schema(
        description = "Confirmação de que o estorno do adiantamento foi realizado. Obrigatório quando o pedido possuir valorAdiantamento",
        example = "true"
    )
    private boolean estornoConfirmado;
}
