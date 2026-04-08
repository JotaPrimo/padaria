package padaria.com.example.padaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import padaria.com.example.padaria.dto.pedido.PagamentoRequestDTO;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
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
@DisplayName("PagamentoService — Testes de Integração")
class PagamentoServiceTest {

    @Autowired private IPagamentoService pagamentoService;
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

    // ==================== REGISTRAR ====================

    @Test
    @DisplayName("Registrar pagamento com dados válidos deve persistir e retornar DTO")
    void registrar_comDadosValidos_persistePagamentoERetornaDTO() {
        var pedido = criarPedidoNoBanco(StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var dto = criarRequestDTO(new BigDecimal("150.00"));

        var resultado = pagamentoService.registrar(pedido.getId(), dto, usuarioLogado);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultado.isValido()).isTrue();
        assertThat(resultado.getRegistradoPor()).isEqualTo(usuarioLogado.getNome());
        assertThat(resultado.getDataRegistro()).isNotNull();
    }

    @Test
    @DisplayName("Registrar pagamento em pedido inexistente deve lançar RecursoNaoEncontradoException")
    void registrar_pedidoInexistente_lancaRecursoNaoEncontradoException() {
        var dto = criarRequestDTO(new BigDecimal("100.00"));

        assertThatThrownBy(() -> pagamentoService.registrar(99999L, dto, usuarioLogado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Registrar pagamento em pedido cancelado deve lançar NegocioException")
    void registrar_pedidoCancelado_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(StatusPedido.CANCELADO, new BigDecimal("350.00"));
        var dto = criarRequestDTO(new BigDecimal("100.00"));

        assertThatThrownBy(() -> pagamentoService.registrar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("cancelado");
    }

    @Test
    @DisplayName("Registrar pagamento com valor que ultrapassa total do pedido deve lançar NegocioException")
    void registrar_somaSuperaValorPedido_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var dto = criarRequestDTO(new BigDecimal("400.00"));

        assertThatThrownBy(() -> pagamentoService.registrar(pedido.getId(), dto, usuarioLogado))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    @DisplayName("Pagamento inválido não deve ser contabilizado no total — deve permitir novo registro")
    void registrar_pagamentoInvalidoNaoContabilizadoNoTotal() {
        var pedido = criarPedidoNoBanco(StatusPedido.PENDENTE, new BigDecimal("350.00"));
        // Cria pagamento inválido de 300 — se fosse contabilizado, bloquearia novo pagamento de 300
        criarPagamentoNoBanco(pedido, new BigDecimal("300.00"), false);

        var dto = criarRequestDTO(new BigDecimal("300.00"));
        var resultado = pagamentoService.registrar(pedido.getId(), dto, usuarioLogado);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    // ==================== INVALIDAR ====================

    @Test
    @DisplayName("Invalidar pagamento válido deve marcar como inválido e registrar data e responsável")
    void invalidar_comDadosValidos_marcaPagamentoComoInvalido() {
        var pedido = criarPedidoNoBanco(StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var pagamento = criarPagamentoNoBanco(pedido, new BigDecimal("100.00"), true);

        var resultado = pagamentoService.invalidar(pagamento.getId(), usuarioLogado);

        assertThat(resultado.isValido()).isFalse();
        assertThat(resultado.getInvaliadoEm()).isNotNull();
        assertThat(resultado.getInvaliadoPor()).isEqualTo(usuarioLogado.getNome());
    }

    @Test
    @DisplayName("Invalidar pagamento inexistente deve lançar RecursoNaoEncontradoException")
    void invalidar_pagamentoInexistente_lancaRecursoNaoEncontradoException() {
        assertThatThrownBy(() -> pagamentoService.invalidar(99999L, usuarioLogado))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Invalidar pagamento já invalidado deve lançar NegocioException")
    void invalidar_pagamentoJaInvalido_lancaNegocioException() {
        var pedido = criarPedidoNoBanco(StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var pagamento = criarPagamentoNoBanco(pedido, new BigDecimal("100.00"), false);

        assertThatThrownBy(() -> pagamentoService.invalidar(pagamento.getId(), usuarioLogado))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("invalidado");
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

    private Pedido criarPedidoNoBanco(StatusPedido status, BigDecimal valorPedido) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Teste");
        pedido.setTelefone("(11) 99999-8888");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(7));
        pedido.setDescricaoPedido("Bolo de aniversário para 50 pessoas");
        pedido.setStatusPedido(status);
        pedido.setValorPedido(valorPedido);
        pedido.setPagamentoIntegral(true);
        pedido.setCadastradoPor(usuarioLogado);
        pedido.setAlteradoPor(usuarioLogado);
        return pedidoRepository.save(pedido);
    }

    private Pagamento criarPagamentoNoBanco(Pedido pedido, BigDecimal valor, boolean valido) {
        var pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValor(valor);
        pagamento.setRegistradoPor(usuarioLogado);
        pagamento.setValido(valido);
        if (!valido) {
            pagamento.setInvaliadoEm(LocalDateTime.now());
            pagamento.setInvaliadoPor(usuarioLogado);
        }
        return pagamentoRepository.save(pagamento);
    }

    private PagamentoRequestDTO criarRequestDTO(BigDecimal valor) {
        try {
            var constructor = PagamentoRequestDTO.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var dto = constructor.newInstance();
            var field = PagamentoRequestDTO.class.getDeclaredField("valor");
            field.setAccessible(true);
            field.set(dto, valor);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar PagamentoRequestDTO via reflection", e);
        }
    }
}
