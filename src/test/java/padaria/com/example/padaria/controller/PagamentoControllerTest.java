package padaria.com.example.padaria.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import padaria.com.example.padaria.base.IntegrationTestBase;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.repository.PagamentoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PagamentoController — Testes de Integração")
class PagamentoControllerTest extends IntegrationTestBase {

    @Autowired private PagamentoRepository pagamentoRepository;

    // ==================== REGISTRAR ====================

    @Test
    @DisplayName("Registrar pagamento sem token deve retornar 401")
    void registrar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", 100.00))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registrar pagamento com dados válidos deve retornar 201")
    void registrar_comDadosValidos_retorna201() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", 150.00))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valor").value(150.00))
                .andExpect(jsonPath("$.data.valido").value(true));
    }

    @Test
    @DisplayName("Registrar pagamento em pedido inexistente deve retornar 404")
    void registrar_pedidoInexistente_retorna404() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", 99999L)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", 100.00))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Registrar pagamento em pedido cancelado deve retornar 400")
    void registrar_pedidoCancelado_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.CANCELADO, new BigDecimal("350.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", 100.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Registrar pagamento sem campo valor deve retornar 400 com erro por campo")
    void registrar_semCampoValor_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.valor").exists());
    }

    @Test
    @DisplayName("Registrar pagamento com valor negativo deve retornar 400")
    void registrar_valorNegativo_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", -50.00))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registrar pagamento com valor que ultrapassa o total do pedido deve retornar 400")
    void registrar_somaSuperaValorPedido_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("valor", 500.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== INVALIDAR ====================

    @Test
    @DisplayName("Invalidar pagamento sem token deve retornar 401")
    void invalidar_semToken_retorna401() throws Exception {
        mockMvc.perform(patch("/api/v1/pagamentos/{id}/invalidar", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalidar pagamento com perfil FUNCIONARIO deve retornar 403")
    void invalidar_comFuncionario_retorna403() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var pagamento = criarPagamentoNoBanco(pedido, admin, new BigDecimal("100.00"));
        String token = obterTokenFuncionario();

        mockMvc.perform(patch("/api/v1/pagamentos/{id}/invalidar", pagamento.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Invalidar pagamento com perfil ADMIN deve retornar 200 com valido=false")
    void invalidar_comAdmin_retorna200() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var pagamento = criarPagamentoNoBanco(pedido, admin, new BigDecimal("100.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pagamentos/{id}/invalidar", pagamento.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valido").value(false));
    }

    @Test
    @DisplayName("Invalidar pagamento inexistente deve retornar 404")
    void invalidar_pagamentoInexistente_retorna404() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pagamentos/{id}/invalidar", 99999L)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Invalidar pagamento já invalidado deve retornar 400")
    void invalidar_pagamentoJaInvalido_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE, new BigDecimal("350.00"));
        var pagamento = criarPagamentoInvalidoNoBanco(pedido, admin, new BigDecimal("100.00"));
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pagamentos/{id}/invalidar", pagamento.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== HELPERS ====================

    private Pedido criarPedidoNoBanco(Usuario usuario, StatusPedido status, BigDecimal valorPedido) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Teste");
        pedido.setTelefone("(11) 99999-8888");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(7));
        pedido.setDescricaoPedido("Bolo de aniversário para 50 pessoas");
        pedido.setStatusPedido(status);
        pedido.setValorPedido(valorPedido);
        pedido.setPagamentoIntegral(true);
        pedido.setCadastradoPor(usuario);
        pedido.setAlteradoPor(usuario);
        return pedidoRepository.save(pedido);
    }

    private Pagamento criarPagamentoNoBanco(Pedido pedido, Usuario registradoPor, BigDecimal valor) {
        var pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValor(valor);
        pagamento.setRegistradoPor(registradoPor);
        return pagamentoRepository.save(pagamento);
    }

    private Pagamento criarPagamentoInvalidoNoBanco(Pedido pedido, Usuario registradoPor, BigDecimal valor) {
        var pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValor(valor);
        pagamento.setRegistradoPor(registradoPor);
        pagamento.setValido(false);
        pagamento.setInvaliadoEm(LocalDateTime.now());
        pagamento.setInvaliadoPor(registradoPor);
        return pagamentoRepository.save(pagamento);
    }
}
