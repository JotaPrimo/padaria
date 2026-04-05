package padaria.com.example.padaria.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import padaria.com.example.padaria.dto.pedido.PagamentoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Pagamento;
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
        expression = "java(pedido.getPagamentos().stream().filter(p -> p.isValido()).map(p -> p.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add))")
    @Mapping(source = "pagamentos", target = "pagamentos")
    PedidoResponseDTO toResponseDTO(Pedido pedido);

    @Mapping(source = "registradoPor.nome", target = "registradoPor")
    @Mapping(source = "invaliadoPor.nome",  target = "invaliadoPor")
    PagamentoResponseDTO toResponseDTO(Pagamento pagamento);

    @Mapping(target = "valorAdiantamento",
        expression = "java(dto.getPagamentoIntegral() ? null : dto.getValorAdiantamento())")
    @Mapping(target = "statusPedido",        ignore = true)
    @Mapping(target = "cadastradoPor",       ignore = true)
    @Mapping(target = "alteradoPor",         ignore = true)
    @Mapping(target = "dataUltimaAlteracao", ignore = true)
    @Mapping(target = "dataCriacao",         ignore = true)
    @Mapping(target = "dataCancelamento",    ignore = true)
    @Mapping(target = "motivoCancelamento",  ignore = true)
    @Mapping(target = "obsCancelamento",     ignore = true)
    @Mapping(target = "pagamentos",          ignore = true)
    Pedido toEntity(PedidoRequestDTO dto);

    @Mapping(target = "valorAdiantamento",   ignore = true)
    @Mapping(target = "cliente",             ignore = true)
    @Mapping(target = "cadastradoPor",       ignore = true)
    @Mapping(target = "alteradoPor",         ignore = true)
    @Mapping(target = "dataUltimaAlteracao", ignore = true)
    @Mapping(target = "dataCriacao",         ignore = true)
    @Mapping(target = "dataCancelamento",    ignore = true)
    @Mapping(target = "motivoCancelamento",  ignore = true)
    @Mapping(target = "obsCancelamento",     ignore = true)
    @Mapping(target = "pagamentos",          ignore = true)
    void updateFromDTO(PedidoUpdateDTO dto, @MappingTarget Pedido pedido);

    @Mapping(target = "motivoCancelamento", source = "motivoCancelamento")
    @Mapping(target = "obsCancelamento",    source = "obsCancelamento")
    @Mapping(target = "cliente",             ignore = true)
    @Mapping(target = "telefone",            ignore = true)
    @Mapping(target = "dataHoraEntrega",     ignore = true)
    @Mapping(target = "descricaoPedido",     ignore = true)
    @Mapping(target = "observacao",          ignore = true)
    @Mapping(target = "statusPedido",        ignore = true)
    @Mapping(target = "valorPedido",         ignore = true)
    @Mapping(target = "pagamentoIntegral",   ignore = true)
    @Mapping(target = "valorAdiantamento",   ignore = true)
    @Mapping(target = "dataCancelamento",    ignore = true)
    @Mapping(target = "cadastradoPor",       ignore = true)
    @Mapping(target = "alteradoPor",         ignore = true)
    @Mapping(target = "dataUltimaAlteracao", ignore = true)
    @Mapping(target = "dataCriacao",         ignore = true)
    @Mapping(target = "pagamentos",          ignore = true)
    void applyCancelamento(PedidoCancelamentoDTO dto, @MappingTarget Pedido pedido);
}
