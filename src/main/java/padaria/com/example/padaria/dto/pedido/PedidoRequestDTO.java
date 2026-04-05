package padaria.com.example.padaria.dto.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Schema(description = "Dados para cadastro de um novo pedido")
public class PedidoRequestDTO {

    @NotBlank(message = "O campo cliente é obrigatório.")
    @Size(max = 255, message = "O campo cliente deve ter no máximo 255 caracteres.")
    @Schema(description = "Nome do cliente que realizou o pedido", example = "Maria Oliveira", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cliente;

    @NotBlank(message = "O campo telefone é obrigatório.")
    @Size(max = 20, message = "O campo telefone deve ter no máximo 20 caracteres.")
    @Schema(description = "Número de telefone para contato com o cliente", example = "(11) 99999-8888", requiredMode = Schema.RequiredMode.REQUIRED)
    private String telefone;

    @NotNull(message = "O campo data/hora de entrega é obrigatório.")
    @Schema(description = "Data e hora acordados para a entrega da encomenda", example = "2025-12-24T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime dataHoraEntrega;

    @NotBlank(message = "O campo descrição do pedido é obrigatório.")
    @Schema(description = "Descrição detalhada do pedido para orientar a equipe de produção", example = "Bolo de chocolate com cobertura de brigadeiro, 2 andares, para 50 pessoas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descricaoPedido;

    @Schema(description = "Informações adicionais livres, como bilhetes especiais ou instruções extras", example = "Escrever 'Feliz Aniversário João' no topo", nullable = true)
    private String observacao;

    @NotNull(message = "O campo valor do pedido é obrigatório.")
    @Positive(message = "O valor do pedido deve ser maior que zero.")
    @Schema(description = "Valor total cobrado pelo pedido", example = "350.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorPedido;

    @NotNull(message = "O campo pagamento integral é obrigatório.")
    @Schema(description = "Indica se o pagamento foi realizado integralmente. Se false, o campo valorAdiantamento torna-se obrigatório", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean pagamentoIntegral;

    @Positive(message = "O valor do adiantamento deve ser maior que zero.")
    @Schema(description = "Valor pago antecipadamente pelo cliente. Obrigatório quando pagamentoIntegral for false", example = "150.00", nullable = true)
    private BigDecimal valorAdiantamento;
}
