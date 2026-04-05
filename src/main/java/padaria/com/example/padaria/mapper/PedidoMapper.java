package padaria.com.example.padaria.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {StatusPedido.class, LocalDateTime.class, BigDecimal.class})
public interface PedidoMapper {

    @Mapping(source = "cadastradoPor.nome", target = "cadastradoPor")
    @Mapping(source = "alteradoPor.nome",   target = "alteradoPor")
    @Mapping(target = "atrasado",
        expression = "java(pedido.getStatusPedido() == StatusPedido.PENDENTE && pedido.getDataHoraEntrega().isBefore(LocalDateTime.now()))")
    @Mapping(target = "totalPagamentos",
        expression = "java(pedido.isPagamentoIntegral() ? pedido.getValorPedido() : (pedido.getValorAdiantamento() != null ? pedido.getValorAdiantamento() : BigDecimal.ZERO))")
    PedidoResponseDTO toResponseDTO(Pedido pedido);
}
