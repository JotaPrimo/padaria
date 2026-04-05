package padaria.com.example.padaria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import padaria.com.example.padaria.dto.pedido.PagamentoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoResponseDTO;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.mapper.PedidoMapper;
import padaria.com.example.padaria.repository.PagamentoPedidoRepository;
import padaria.com.example.padaria.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagamentoServiceImpl implements IPagamentoService {

    private final PedidoRepository pedidoRepository;
    private final PagamentoPedidoRepository pagamentoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    @Transactional
    public PagamentoResponseDTO registrar(Long pedidoId, PagamentoRequestDTO dto, Usuario usuarioLogado) {
        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

        if (pedido.isPedidoCancelado()) {
            throw new NegocioException("Não é possível registrar pagamento em um pedido cancelado.");
        }

        BigDecimal totalValido = pedido.getPagamentos().stream()
                .filter(Pagamento::isValido)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValido.add(dto.getValor()).compareTo(pedido.getValorPedido()) > 0) {
            throw new NegocioException(
                    "O valor informado ultrapassa o total do pedido. "
                    + "Total já pago: R$ " + totalValido
                    + ". Valor máximo a receber: R$ " + pedido.getValorPedido().subtract(totalValido) + "."
            );
        }

        var pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValor(dto.getValor());
        pagamento.setRegistradoPor(usuarioLogado);

        return pedidoMapper.toResponseDTO(pagamentoRepository.save(pagamento));
    }

    @Override
    @Transactional
    public PagamentoResponseDTO invalidar(Long pagamentoId, Usuario usuarioLogado) {
        var pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado."));

        if (!pagamento.isValido()) {
            throw new NegocioException("Este pagamento já está invalidado.");
        }

        pagamento.setValido(false);
        pagamento.setInvaliadoEm(LocalDateTime.now());
        pagamento.setInvaliadoPor(usuarioLogado);

        return pedidoMapper.toResponseDTO(pagamentoRepository.save(pagamento));
    }
}
