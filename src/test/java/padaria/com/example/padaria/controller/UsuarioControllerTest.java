package padaria.com.example.padaria.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import padaria.com.example.padaria.base.IntegrationTestBase;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UsuarioController — Testes de Integração")
class UsuarioControllerTest extends IntegrationTestBase {

    // ==================== LISTAR ====================

    @Test
    @DisplayName("Listar usuários sem token deve retornar 401")
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listar usuários com token de funcionário deve retornar 200")
    void listar_comTokenFuncionario_retorna200() throws Exception {
        String token = obterTokenFuncionario();

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== BUSCAR POR ID ====================

    @Test
    @DisplayName("Buscar usuário por ID com perfil FUNCIONARIO deve retornar 403")
    void buscarPorId_comFuncionario_retorna403() throws Exception {
        String token = obterTokenFuncionario();
        Long adminId = usuarioRepository.findByEmail(ADMIN_EMAIL).get().getId();

        mockMvc.perform(get("/api/v1/usuarios/{id}", adminId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Buscar usuário por ID inexistente deve retornar 404")
    void buscarPorId_idInexistente_retorna404() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/v1/usuarios/{id}", 99999L)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Buscar usuário por ID com ADMIN deve retornar 200 com os dados")
    void buscarPorId_comAdmin_retorna200() throws Exception {
        String token = obterTokenAdmin();
        Long adminId = usuarioRepository.findByEmail(ADMIN_EMAIL).get().getId();

        mockMvc.perform(get("/api/v1/usuarios/{id}", adminId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.role").value("ADMINISTRADOR"));
    }

    // ==================== CRIAR ====================

    @Test
    @DisplayName("Criar usuário com perfil FUNCIONARIO deve retornar 403")
    void criar_comFuncionario_retorna403() throws Exception {
        String token = obterTokenFuncionario();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(novoUsuarioValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Criar usuário com dados válidos deve retornar 201")
    void criar_comDadosValidos_retorna201() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(novoUsuarioValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("novo@padaria.com"))
                .andExpect(jsonPath("$.data.role").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.data.ativo").value(true));
    }

    @Test
    @DisplayName("Criar usuário com email já cadastrado deve retornar 400")
    void criar_comEmailDuplicado_retorna400() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "nome", "Outro Admin",
                                "email", ADMIN_EMAIL,
                                "senha", "Admin@1234",
                                "role", "ADMINISTRADOR"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Criar usuário com senha fraca deve retornar 400 com erro no campo senha")
    void criar_comSenhaFraca_retorna400() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "nome", "Usuario Teste",
                                "email", "teste@padaria.com",
                                "senha", "123",
                                "role", "FUNCIONARIO"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.senha").exists());
    }

    @Test
    @DisplayName("Criar usuário com campos obrigatórios ausentes deve retornar 400")
    void criar_semCamposObrigatorios_retorna400() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.senha").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    // ==================== ATUALIZAR ====================

    @Test
    @DisplayName("Atualizar usuário com dados válidos deve retornar 200")
    void atualizar_comDadosValidos_retorna200() throws Exception {
        String token = obterTokenAdmin();
        Long adminId = usuarioRepository.findByEmail(ADMIN_EMAIL).get().getId();

        mockMvc.perform(put("/api/v1/usuarios/{id}", adminId)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "nome", "Admin Atualizado",
                                "email", ADMIN_EMAIL,
                                "role", "ADMINISTRADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nome").value("Admin Atualizado"));
    }

    @Test
    @DisplayName("Atualizar usuário com email já em uso deve retornar 400")
    void atualizar_comEmailJaEmUso_retorna400() throws Exception {
        criarFuncionario();
        String token = obterTokenAdmin();
        Long adminId = usuarioRepository.findByEmail(ADMIN_EMAIL).get().getId();

        mockMvc.perform(put("/api/v1/usuarios/{id}", adminId)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "nome", "Admin Teste",
                                "email", FUNCIONARIO_EMAIL,
                                "role", "ADMINISTRADOR"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== INATIVAR ====================

    @Test
    @DisplayName("Inativar usuário ativo deve retornar 200")
    void inativar_usuarioAtivo_retorna200() throws Exception {
        String token = obterTokenAdmin();
        var funcionario = criarFuncionario();

        mockMvc.perform(patch("/api/v1/usuarios/{id}/inativar", funcionario.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Inativar usuário já inativo deve retornar 400")
    void inativar_usuarioJaInativo_retorna400() throws Exception {
        String token = obterTokenAdmin();
        var inativo = criarFuncionarioInativo();

        mockMvc.perform(patch("/api/v1/usuarios/{id}/inativar", inativo.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== REATIVAR ====================

    @Test
    @DisplayName("Reativar usuário inativo deve retornar 200")
    void reativar_usuarioInativo_retorna200() throws Exception {
        String token = obterTokenAdmin();
        var inativo = criarFuncionarioInativo();

        mockMvc.perform(patch("/api/v1/usuarios/{id}/reativar", inativo.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Reativar usuário já ativo deve retornar 400")
    void reativar_usuarioJaAtivo_retorna400() throws Exception {
        String token = obterTokenAdmin();
        var funcionario = criarFuncionario();

        mockMvc.perform(patch("/api/v1/usuarios/{id}/reativar", funcionario.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== HELPERS ====================

    private Map<String, String> novoUsuarioValido() {
        return Map.of(
                "nome", "Novo Funcionario",
                "email", "novo@padaria.com",
                "senha", "Novo@1234",
                "role", "FUNCIONARIO"
        );
    }
}
