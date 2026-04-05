package padaria.com.example.padaria.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import padaria.com.example.padaria.base.IntegrationTestBase;
import padaria.com.example.padaria.entity.Pagamento;
import padaria.com.example.padaria.entity.Pedido;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.StatusPedido;
import padaria.com.example.padaria.repository.PagamentoPedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PedidoController — Testes de Integração")
class PedidoControllerTest extends IntegrationTestBase {

    @Autowired private PagamentoPedidoRepository pagamentoPedidoRepository;

    // ==================== LISTAR ====================

    @Test
    @DisplayName("Listar pedidos sem token deve retornar 401")
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listar pedidos com token válido deve retornar 200")
    void listar_comToken_retorna200() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/pedidos")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Listar com filtro de status deve retornar apenas pedidos correspondentes")
    void listar_comFiltroStatus_retornaApenasPendentes() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        criarPedidoNoBanco(admin, StatusPedido.ENTREGUE);

        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/pedidos")
                        .param("statusPedido", "PENDENTE")   // @ModelAttribute lê query params pelo nome do campo
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].statusPedido").value("PENDENTE"));
    }

    // ==================== BUSCAR POR ID ====================

    @Test
    @DisplayName("Buscar pedido por ID existente deve retornar 200 com os dados")
    void buscarPorId_pedidoExistente_retorna200() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/pedidos/{id}", pedido.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cliente").value("Cliente Teste"))
                .andExpect(jsonPath("$.data.statusPedido").value("PENDENTE"));
    }

    @Test
    @DisplayName("Buscar pedido por ID inexistente deve retornar 404")
    void buscarPorId_pedidoInexistente_retorna404() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/pedidos/{id}", 99999L)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== CRIAR ====================

    @Test
    @DisplayName("Criar pedido com dados válidos deve retornar 201 com status PENDENTE")
    void criar_comDadosValidos_retorna201() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(novoPedidoValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusPedido").value("PENDENTE"))
                .andExpect(jsonPath("$.data.cliente").value("Maria Oliveira"))
                .andExpect(jsonPath("$.data.cadastradoPor").value("Admin Teste"));
    }

    @Test
    @DisplayName("Criar pedido sem campos obrigatórios deve retornar 400 com erros por campo")
    void criar_semCamposObrigatorios_retorna400() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cliente").exists())
                .andExpect(jsonPath("$.errors.telefone").exists())
                .andExpect(jsonPath("$.errors.dataHoraEntrega").exists())
                .andExpect(jsonPath("$.errors.descricaoPedido").exists())
                .andExpect(jsonPath("$.errors.valorPedido").exists())
                .andExpect(jsonPath("$.errors.pagamentoIntegral").exists());
    }

    @Test
    @DisplayName("Criar pedido com pagamento não integral sem adiantamento deve retornar 400")
    void criar_pagamentoNaoIntegralSemAdiantamento_retorna400() throws Exception {
        String token = obterTokenAdmin();

        var payload = new HashMap<>(novoPedidoValido());
        payload.put("pagamentoIntegral", false);
        payload.remove("valorAdiantamento");

        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== ATUALIZAR ====================

    @Test
    @DisplayName("Atualizar pedido com dados válidos deve retornar 200")
    void atualizar_comDadosValidos_retorna200() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenAdmin();

        mockMvc.perform(put("/api/v1/pedidos/{id}", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoPedidoValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.telefone").value("(11) 11111-2222"));
    }

    @Test
    @DisplayName("Atualizar pedido não deve permitir alterar o nome do cliente")
    void atualizar_clienteProtegido_clientePermaneceOriginal() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenAdmin();

        mockMvc.perform(put("/api/v1/pedidos/{id}", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoPedidoValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cliente").value("Cliente Teste")); // nome original, não alterado
    }

    // ==================== CANCELAR ====================

    @Test
    @DisplayName("Cancelar pedido com perfil FUNCIONARIO deve retornar 403")
    void cancelar_comFuncionario_retorna403() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenFuncionario();

        mockMvc.perform(patch("/api/v1/pedidos/{id}/cancelar", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("motivoCancelamento", "CANCELADO_PELO_CLIENTE", "estornoConfirmado", false))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Cancelar pedido com motivo OUTRO sem observação deve retornar 400")
    void cancelar_motivoOutroSemObs_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pedidos/{id}/cancelar", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("motivoCancelamento", "OUTRO", "estornoConfirmado", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Cancelar pedido com adiantamento sem confirmar estorno deve retornar 400")
    void cancelar_comAdiantamentoSemEstornoConfirmado_retorna400() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoComAdiantamentoNoBanco(admin);
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pedidos/{id}/cancelar", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("motivoCancelamento", "CANCELADO_PELO_CLIENTE", "estornoConfirmado", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Cancelar pedido válido com ADMIN deve retornar 200")
    void cancelar_pedidoValido_comAdmin_retorna200() throws Exception {
        var admin = usuarioRepository.findByEmail(ADMIN_EMAIL).get();
        var pedido = criarPedidoNoBanco(admin, StatusPedido.PENDENTE);
        String token = obterTokenAdmin();

        mockMvc.perform(patch("/api/v1/pedidos/{id}/cancelar", pedido.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("motivoCancelamento", "CANCELADO_PELO_CLIENTE", "estornoConfirmado", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== DASHBOARD ====================

    @Test
    @DisplayName("Dashboard deve retornar 200 com os quatro contadores")
    void dashboard_retorna200ComContadores() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/pedidos/dashboard")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pedidosHoje").exists())
                .andExpect(jsonPath("$.data.pedidosSemana").exists())
                .andExpect(jsonPath("$.data.pedidosMes").exists())
                .andExpect(jsonPath("$.data.pedidosPagamentoPendente").exists());
    }

    // ==================== HELPERS ====================

    private Pedido criarPedidoNoBanco(Usuario usuario, StatusPedido status) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Teste");
        pedido.setTelefone("(11) 99999-8888");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(7));
        pedido.setDescricaoPedido("Bolo de chocolate para 50 pessoas");
        pedido.setStatusPedido(status);
        pedido.setValorPedido(new BigDecimal("350.00"));
        pedido.setPagamentoIntegral(true);
        pedido.setCadastradoPor(usuario);
        pedido.setAlteradoPor(usuario);
        return pedidoRepository.save(pedido);
    }

    private Pedido criarPedidoComAdiantamentoNoBanco(Usuario usuario) {
        var pedido = new Pedido();
        pedido.setCliente("Cliente Adiantamento");
        pedido.setTelefone("(11) 99999-0000");
        pedido.setDataHoraEntrega(LocalDateTime.now().plusDays(5));
        pedido.setDescricaoPedido("Torta com adiantamento registrado");
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
        pagamentoPedidoRepository.save(pagamento);

        return pedidoSalvo;
    }

    private Map<String, Object> novoPedidoValido() {
        return Map.of(
                "cliente", "Maria Oliveira",
                "telefone", "(11) 98765-4321",
                "dataHoraEntrega", "2025-12-31T10:00:00",
                "descricaoPedido", "Bolo de chocolate decorado para 30 pessoas",
                "valorPedido", 350.00,
                "pagamentoIntegral", true
        );
    }

    private Map<String, Object> atualizacaoPedidoValida() {
        return Map.of(
                "telefone", "(11) 11111-2222",
                "dataHoraEntrega", "2025-12-31T14:00:00",
                "descricaoPedido", "Bolo atualizado para 40 pessoas",
                "statusPedido", "PENDENTE",
                "valorPedido", 400.00,
                "pagamentoIntegral", true
        );
    }
}
