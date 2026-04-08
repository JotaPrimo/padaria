package padaria.com.example.padaria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import padaria.com.example.padaria.dto.pedido.DashboardResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.mapper.PedidoMapper;
import padaria.com.example.padaria.repository.PagamentoRepository;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.utils.StringValidator;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listar(PedidoFiltroDTO filtro, Pageable pageable) {
        return pedidoRepository.findAll(PedidoFilterSpec.comFiltros(filtro), pageable)
                .map(pedidoMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        return pedidoMapper.toResponseDTO(buscarOuLancar(id));
    }

    @Override
    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto, Usuario usuarioLogado) {
        validarAdiantamentoObrigatorio(dto);

        var pedido = pedidoMapper.toEntity(dto);
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setCadastradoPor(usuarioLogado);
        pedido.setAlteradoPor(usuarioLogado);

        var pedidoSalvo = pedidoRepository.save(pedido);

        var pagamentoInicial = new Pagamento();
        pagamentoInicial.setPedido(pedidoSalvo);
        pagamentoInicial.setRegistradoPor(usuarioLogado);
        if (dto.getPagamentoIntegral()) {
            pagamentoInicial.setValor(dto.getValorPedido());
        } else {
            pagamentoInicial.setValor(dto.getValorAdiantamento());
        }
        pagamentoRepository.save(pagamentoInicial);

        return pedidoMapper.toResponseDTO(pedidoRepository.findById(pedidoSalvo.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public PedidoResponseDTO atualizar(Long id, PedidoUpdateDTO dto, Usuario usuarioLogado) {
        var pedido = buscarOuLancar(id);

        bloquearEdicaoPedidoCancelado(pedido);

        pedidoMapper.updateFromDTO(dto, pedido);
        pedido.setAlteradoPor(usuarioLogado);
        pedido.setDataUltimaAlteracao(LocalDateTime.now());

        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public void cancelar(Long id, PedidoCancelamentoDTO dto, Usuario usuarioLogado) {
        var pedido = buscarOuLancar(id);

        verificarPedidoJaCancelado(pedido);
        validarObservacaoCancelamentoObrigatoria(dto);
        checarEstorno(dto, pedido);

        pedidoMapper.applyCancelamento(dto, pedido);
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedido.setDataCancelamento(LocalDate.now());
        pedido.setAlteradoPor(usuarioLogado);

        pedidoRepository.save(pedido);
    }

    @Override
    public DashboardResponseDTO dashboard() {

        LocalDate hojeDate = LocalDate.now();

        LocalDateTime hoje = hojeDate.atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1);

        LocalDateTime inicioSemana = hojeDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .atStartOfDay();
        LocalDateTime fimSemana = inicioSemana.plusDays(7);

        LocalDateTime inicioMes = hojeDate
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();
        LocalDateTime fimMes = inicioMes.plusMonths(1);

        long pedidosHoje = pedidoRepository.countByDataHoraEntregaBetween(hoje, fimHoje);
        long pedidosSemana = pedidoRepository.countByDataHoraEntregaBetween(inicioSemana, fimSemana);
        long pedidosMes = pedidoRepository.countByDataHoraEntregaBetween(inicioMes, fimMes);
        long pagamentoPendente = pedidoRepository.countPagamentoPendente(StatusPedido.CANCELADO);

        return new DashboardResponseDTO(pedidosHoje, pedidosSemana, pedidosMes, pagamentoPendente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> exportar(PedidoFiltroDTO filtro) {
        var sort = Sort.by(Sort.Direction.ASC, "dataHoraEntrega");
        return pedidoRepository.findAll(PedidoFilterSpec.comFiltros(filtro), sort)
                .stream()
                .map(pedidoMapper::toResponseDTO)
                .toList();
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
        BigDecimal totalPago = pedido.getPagamentos().stream()
                .filter(Pagamento::getValido)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPago.compareTo(BigDecimal.ZERO) > 0 && !dto.isEstornoConfirmado()) {
            throw new NegocioException(
                    "Este pedido possui adiantamento de R$ " + totalPago
                    + ". Confirme que o estorno foi realizado antes de cancelar."
            );
        }
    }
}
