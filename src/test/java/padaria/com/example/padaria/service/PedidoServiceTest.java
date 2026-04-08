package padaria.com.example.padaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.MotivoCancelamento;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.PagamentoRepository;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PedidoService — Testes de Integração")
class PedidoServiceTest {

    @Autowired private IPedidoService pedidoService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario usuarioLogado;

    @BeforeEach
    void configurarBanco() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        usuarioLogado = criarUsuarioNoBanco("admin@padaria.com", Role.ADMINISTRADOR);
    }

    // ==================== CRIAR ====================

    @Test
    @DisplayName("Criar pedido com dados válidos deve persistir com status PENDENTE e cadastradoPor preenchido")
    void criar_comDadosValidos_statusPendenteECadastradoPorPreenchido() {
        var dto = criarRequestDTO("Maria Oliveira", "(11) 99999-8888",
                LocalDateTime.now().plusDays(7), "Bolo de chocolate", true, null);

        var resultado = pedidoService.criar(dto, usuarioLogado);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatusPedido()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(resultado.getCadastradoPor()).isEqualTo(usuarioLogado.getNome());
        assertThat(resultado.getAlteradoPor()).isEqualTo(usuarioLogado.getNome());
        assertThat(resultado.getCliente()).isEqualTo("Maria Oliveira");
    }

    @Test
    @DisplayName("Criar pedido com pagamento não integral sem adiantamento deve lançar NegocioException")
    void criar_pagamentoNaoIntegralSemAdiantamento_lancaNegocioException() {
        var dto = criarRequestDTO("João", "(11) 99999-0000",
                LocalDateTime.now().plusDays(3), "Pão especial", false, null);

        assertThatThrownBy(() -> pedidoService.criar(dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("adiantamento");
    }

    @Test
    @DisplayName("Criar pedido com pagamento integral deve ignorar valorAdiantamento")
    void criar_pagamentoIntegral_valorAdiantamentoDeveSerNulo() {
        var dto = criarRequestDTO("Ana", "(11) 88888-8888",
                LocalDateTime.now().plusDays(5), "Torta de morango", true, new BigDecimal("50.00"));

        var resultado = pedidoService.criar(dto, usuarioLogado);

        assertThat(resultado.getValorAdiantamento()).isNull();
    }

    // ==================== BUSCAR POR ID ====================

    @Test
    @DisplayName("Buscar pedido por ID existente deve retornar o DTO")
    void buscarPorId_idExistente_retornaDTO() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.PENDENTE);

        var resultado = pedidoService.buscarPorId(pedido.getId());

        assertThat(resultado.getId()).isEqualTo(pedido.getId());
        assertThat(resultado.getCliente()).isEqualTo("Cliente Teste");
    }

    @Test
    @DisplayName("Buscar pedido por ID inexistente deve lançar RecursoNaoEncontradoException")
    void buscarPorId_idInexistente_lancaRecursoNaoEncontradoException() {
        assertThatThrownBy(() -> pedidoService.buscarPorId(99999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ==================== ATUALIZAR ====================

    @Test
    @DisplayName("Atualizar pedido deve persistir alterações e manter o cliente original intocado")
    void atualizar_comDadosValidos_clienteNaoAlterado() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.PENDENTE);
        var dto = criarUpdateDTO("(11) 11111-2222", LocalDateTime.now().plusDays(10),
                "Bolo atualizado", StatusPedido.ENTREGUE, true);

        var resultado = pedidoService.atualizar(pedido.getId(), dto, usuarioLogado);

        assertThat(resultado.getTelefone()).isEqualTo("(11) 11111-2222");
        assertThat(resultado.getStatusPedido()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(resultado.getCliente()).isEqualTo("Cliente Teste"); // protegido
        assertThat(resultado.getAlteradoPor()).isEqualTo(usuarioLogado.getNome());
    }

    @Test
    @DisplayName("Atualizar pedido cancelado deve lançar NegocioException")
    void atualizar_pedidoCancelado_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.CANCELADO);
        var dto = criarUpdateDTO("(11) 11111-2222", LocalDateTime.now().plusDays(10),
                "Desc", StatusPedido.PENDENTE, true);

        assertThatThrownBy(() -> pedidoService.atualizar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelado");
    }

    // ==================== CANCELAR ====================

    @Test
    @DisplayName("Cancelar pedido pendente deve setar status CANCELADO e registrar data")
    void cancelar_comDadosValidos_setaStatusCancelado() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.PENDENTE);
        var dto = criarCancelamentoDTO(MotivoCancelamento.CANCELADO_PELO_CLIENTE, null, false);

        pedidoService.cancelar(pedido.getId(), dto, usuarioLogado);

        var atualizado = pedidoRepository.findById(pedido.getId()).get();
        assertThat(atualizado.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(atualizado.getMotivoCancelamento()).isEqualTo(MotivoCancelamento.CANCELADO_PELO_CLIENTE);
        assertThat(atualizado.getDataCancelamento()).isNotNull();
    }

    @Test
    @DisplayName("Cancelar pedido já cancelado deve lançar NegocioException")
    void cancelar_pedidoJaCancelado_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.CANCELADO);
        var dto = criarCancelamentoDTO(MotivoCancelamento.CANCELADO_PELA_PADARIA, null, false);

        assertThatThrownBy(() -> pedidoService.cancelar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelado");
    }

    @Test
    @DisplayName("Cancelar com motivo OUTRO sem observação deve lançar NegocioException")
    void cancelar_motivoOutroSemObs_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(usuarioLogado, StatusPedido.PENDENTE);
        var dto = criarCancelamentoDTO(MotivoCancelamento.OUTRO, null, false);

        assertThatThrownBy(() -> pedidoService.cancelar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("observação");
    }

    @Test
    @DisplayName("Cancelar pedido com adiantamento sem confirmar estorno deve lançar NegocioException")
    void cancelar_comAdiantamentoSemEstorno_lancaNegocioException() {
        var pedido = criarPedidoComAdiantamentoNoBanco(usuarioLogado);
        var dto = criarCancelamentoDTO(MotivoCancelamento.CANCELADO_PELO_CLIENTE, null, false);

        assertThatThrownBy(() -> pedidoService.cancelar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("adiantamento");
    }

    // ==================== DASHBOARD ====================

    @Test
    @DisplayName("Dashboard deve retornar contadores refletindo pedidos existentes")
    void dashboard_retornaContadoresCorretos() {
        // Dois pedidos para hoje — garantidamente dentro do dia, semana e mês
        criarPedidoNoBancoParaHoje(usuarioLogado);
        criarPedidoNoBancoParaHoje(usuarioLogado);

        var resultado = pedidoService.dashboard();

        assertThat(resultado.getPedidosHoje()).isGreaterThanOrEqualTo(2);
        assertThat(resultado.getPedidosSemana()).isGreaterThanOrEqualTo(2);
        assertThat(resultado.getPedidosMes()).isGreaterThanOrEqualTo(2);
        assertThat(resultado.getPedidosPagamentoPendente()).isEqualTo(0);
    }

    // ==================== HELPERS ====================

    private Usuario criarUsuarioNoBanco(String email, Role role) {
        var usuario = new Usuario();
        usuario.setNome("Admin Teste");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("Admin@1234"));
        usuario.setRole(role);
        return usuarioRepository.save(usuario);
    }

    private Pedido criarPedidoNoBanco(Usuario usuario, StatusPedido status) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Teste");
        pedido.setTelefone("(11) 99999-8888");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(7));
        pedido.setDescricaoPedido("Bolo de aniversário para 50 pessoas");
        pedido.setStatusPedido(status);
        pedido.setValorPedido(new BigDecimal("350.00"));
        pedido.setPagamentoIntegral(true);
        pedido.setCadastradoPor(usuario);
        pedido.setAlteradoPor(usuario);
        return pedidoRepository.save(pedido);
    }

    private Pedido criarPedidoNoBancoParaHoje(Usuario usuario) {
        var pedido = new Pedido();
        pedido.setCliente("Entrega Hoje");
        pedido.setTelefone("(11) 77777-7777");
        pedido.setDataHoraEntrega(LocalDateTime.now().withHour(14).withMinute(0));
        pedido.setDescricaoPedido("Pão especial para entrega hoje");
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setValorPedido(new BigDecimal("80.00"));
        pedido.setPagamentoIntegral(true);
        pedido.setCadastradoPor(usuario);
        pedido.setAlteradoPor(usuario);
        return pedidoRepository.save(pedido);
    }

    private Pedido criarPedidoComAdiantamentoNoBanco(Usuario usuario) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Adiantamento");
        pedido.setTelefone("(11) 55555-5555");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(3));
        pedido.setDescricaoPedido("Torta com adiantamento");
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setValorPedido(new BigDecimal("200.00"));
        pedido.setPagamentoIntegral(false);
        pedido.setValorAdiantamento(new BigDecimal("100.00"));
        pedido.setCadastradoPor(usuario);
        pedido.setAlteradoPor(usuario);
        var pedidoSalvo = pedidoRepository.save(pedido);

        var pagamento = new Pagamento();
        pagamento.setPedido(pedidoSalvo);
        pagamento.setValor(new BigDecimal("100.00"));
        pagamento.setRegistradoPor(usuario);
        pagamentoRepository.save(pagamento);

        return pedidoSalvo;
    }

    private PedidoRequestDTO criarRequestDTO(String cliente, String telefone,
            LocalDateTime dataEntrega, String descricao,
            boolean pagamentoIntegral, BigDecimal adiantamento) {
        return criarDTO(PedidoRequestDTO.class,
                new String[]{"cliente", "telefone", "dataHoraEntrega", "descricaoPedido",
                        "valorPedido", "pagamentoIntegral", "valorAdiantamento"},
                new Object[]{cliente, telefone, dataEntrega, descricao,
                        new BigDecimal("350.00"), pagamentoIntegral, adiantamento});
    }

    private PedidoUpdateDTO criarUpdateDTO(String telefone, LocalDateTime dataEntrega,
            String descricao, StatusPedido status, boolean pagamentoIntegral) {
        return criarDTO(PedidoUpdateDTO.class,
                new String[]{"telefone", "dataHoraEntrega", "descricaoPedido", "statusPedido",
                        "valorPedido", "pagamentoIntegral"},
                new Object[]{telefone, dataEntrega, descricao, status,
                        new BigDecimal("350.00"), pagamentoIntegral});
    }

    private PedidoCancelamentoDTO criarCancelamentoDTO(MotivoCancelamento motivo,
            String obs, boolean estornoConfirmado) {
        return criarDTO(PedidoCancelamentoDTO.class,
                new String[]{"motivoCancelamento", "obsCancelamento", "estornoConfirmado"},
                new Object[]{motivo, obs, estornoConfirmado});
    }

    private <T> T criarDTO(Class<T> clazz, String[] campos, Object[] valores) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            var dto = constructor.newInstance();

            for (int i = 0; i < campos.length; i++) {
                if (valores[i] != null) {
                    var field = clazz.getDeclaredField(campos[i]);
                    field.setAccessible(true);
                    field.set(dto, valores[i]);
                }
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar DTO via reflection", e);
        }
    }
}
