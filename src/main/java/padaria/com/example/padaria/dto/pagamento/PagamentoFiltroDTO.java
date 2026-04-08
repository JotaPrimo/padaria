package padaria.com.example.padaria.dto.pagamento;

import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoFiltroDTO(
        Long id,

        Pedido pedido,

        BigDecimal valorMin,

        Usuario registradoPor,

        Boolean  adiantamento,

        LocalDateTime dataRegistroInicial,

        Boolean  valido,

        Usuario invaliadoPor
) {
}
