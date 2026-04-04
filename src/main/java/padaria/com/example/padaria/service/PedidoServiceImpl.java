package padaria.com.example.padaria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import padaria.com.example.padaria.dto.pedido.DashboardResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.utils.StringValidator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;

    @Override
    public Page<PedidoResponseDTO> listar(PedidoFiltroDTO filtro, Pageable pageable) {
        return pedidoRepository.findAll(PedidoFilterSpec.comFiltros(filtro), pageable)
                .map(PedidoResponseDTO::de);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        return PedidoResponseDTO.de(buscarOuLancar(id));
    }

    @Override
    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto, Usuario usuarioLogado) {
        validarAdiantamentoObrigatorio(dto);

        var pedido = new Pedido();
        pedido.setCliente(dto.getCliente());
        pedido.setTelefone(dto.getTelefone());
        pedido.setDataHoraEntrega(dto.getDataHoraEntrega());
        pedido.setDescricaoPedido(dto.getDescricaoPedido());
        pedido.setObservacao(dto.getObservacao());
        pedido.setValorPedido(dto.getValorPedido());
        pedido.setPagamentoIntegral(dto.getPagamentoIntegral());
        pedido.setValorAdiantamento(dto.getPagamentoIntegral() ? null : dto.getValorAdiantamento());
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setCadastradoPor(usuarioLogado);
        pedido.setAlteradoPor(usuarioLogado);

        return PedidoResponseDTO.de(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoResponseDTO atualizar(Long id, PedidoUpdateDTO dto, Usuario usuarioLogado) {
        var pedido = buscarOuLancar(id);

        bloquearEdicaoPedidoCancelado(pedido);
        isAdiantamentoObrigatorio(dto);

        pedido.setTelefone(dto.getTelefone());
        pedido.setDataHoraEntrega(dto.getDataHoraEntrega());
        pedido.setDescricaoPedido(dto.getDescricaoPedido());
        pedido.setObservacao(dto.getObservacao());
        pedido.setStatusPedido(dto.getStatusPedido());
        pedido.setValorPedido(dto.getValorPedido());
        pedido.setPagamentoIntegral(dto.getPagamentoIntegral());
        pedido.setValorAdiantamento(dto.getPagamentoIntegral() ? null : dto.getValorAdiantamento());
        pedido.setAlteradoPor(usuarioLogado);

        return PedidoResponseDTO.de(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public void cancelar(Long id, PedidoCancelamentoDTO dto, Usuario usuarioLogado) {
        var pedido = buscarOuLancar(id);

        verificarPedidoJaCancelado(pedido);
        validarObservacaoCancelamentoObrigatoria(dto);
        checarEstorno(dto, pedido);

        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedido.setMotivoCancelamento(dto.getMotivoCancelamento());
        pedido.setObsCancelamento(dto.getObsCancelamento());
        pedido.setDataCancelamento(LocalDate.now());
        pedido.setAlteradoPor(usuarioLogado);

        pedidoRepository.save(pedido);
    }

    @Override
    public DashboardResponseDTO dashboard() {
        LocalDateTime hoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1).minusNanos(1);

        LocalDateTime inicioSemana = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime fimSemana = inicioSemana.plusDays(7).minusNanos(1);

        LocalDateTime inicioMes = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        LocalDateTime fimMes = LocalDate.now()
                .with(TemporalAdjusters.lastDayOfMonth())
                .atTime(23, 59, 59);

        long pedidosHoje = pedidoRepository.countByDataHoraEntregaBetween(hoje, fimHoje);
        long pedidosSemana = pedidoRepository.countByDataHoraEntregaBetween(inicioSemana, fimSemana);
        long pedidosMes = pedidoRepository.countByDataHoraEntregaBetween(inicioMes, fimMes);
        long pagamentoPendente = pedidoRepository.countPagamentoPendente(StatusPedido.CANCELADO);

        return new DashboardResponseDTO(pedidosHoje, pedidosSemana, pedidosMes, pagamentoPendente);
    }

    private Pedido buscarOuLancar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private static void validarAdiantamentoObrigatorio(PedidoRequestDTO dto) {
        if (!dto.getPagamentoIntegral() && dto.getValorAdiantamento() == null) {
            throw new NegocioException("O campo valor de adiantamento é obrigatório quando o pagamento não é integral.");
        }
    }

    private static void bloquearEdicaoPedidoCancelado(Pedido pedido) {
        if (StatusPedido.CANCELADO.equals(pedido.getStatusPedido())) {
            throw new NegocioException("Não é possível editar um pedido cancelado.");
        }
    }

    private static void isAdiantamentoObrigatorio(PedidoUpdateDTO dto) {
        if (!dto.getPagamentoIntegral() && dto.getValorAdiantamento() == null) {
            throw new NegocioException("O campo valor de adiantamento é obrigatório quando o pagamento não é integral.");
        }
    }

    private static void verificarPedidoJaCancelado(Pedido pedido) {
        if (pedido.isPedidoCancelado()) {
            throw new NegocioException("O pedido já está cancelado.");
        }
    }

    private static void validarObservacaoCancelamentoObrigatoria(PedidoCancelamentoDTO dto) {
        boolean isCancelamentoOutro = MotivoCancelamento.OUTRO.equals(dto.getMotivoCancelamento());
        boolean isSemObsCancelamento = StringValidator.isNullOrBlank(dto.getObsCancelamento());

        if (isCancelamentoOutro && isSemObsCancelamento) {
            throw new NegocioException("O campo observação de cancelamento é obrigatório quando o motivo for 'Outro'.");
        }
    }

    private static void checarEstorno(PedidoCancelamentoDTO dto, Pedido pedido) {
        if (pedido.getValorAdiantamento() != null && !dto.isEstornoConfirmado()) {
            throw new NegocioException(
                    "Este pedido possui adiantamento de R$ " + pedido.getValorAdiantamento()
                            + ". Confirme que o estorno foi realizado antes de cancelar."
            );
        }
    }
}
