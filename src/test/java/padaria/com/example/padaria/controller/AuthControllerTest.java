package padaria.com.example.padaria.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import padaria.com.example.padaria.base.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController — Testes de Integração")
class AuthControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Login com credenciais válidas deve retornar 200 com token")
    void login_comCredenciaisValidas_retorna200ComToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", ADMIN_EMAIL, "senha", ADMIN_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.nome").value("Admin Teste"))
                .andExpect(jsonPath("$.data.role").value("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("Login com senha incorreta deve retornar 401")
    void login_comSenhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", ADMIN_EMAIL, "senha", "SenhaErrada@1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Login com usuário inativo deve retornar 401")
    void login_comUsuarioInativo_retorna401() throws Exception {
        criarFuncionarioInativo();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "inativo@padaria.com", "senha", "Inativo@1234"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Login com campos em branco deve retornar 400 com erros de validação")
    void login_comCamposEmBranco_retorna400ComErros() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.senha").exists());
    }

    @Test
    @DisplayName("Login com formato de email inválido deve retornar 400")
    void login_comEmailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "nao-e-um-email", "senha", ADMIN_SENHA))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("Login com método GET deve retornar 405")
    void login_comMetodoGet_retorna405() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false));
    }
}
