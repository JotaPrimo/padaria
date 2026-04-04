package padaria.com.example.padaria.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.repository.UsuarioRepository;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected PedidoRepository pedidoRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected static final String ADMIN_EMAIL = "admin@padaria.com";
    protected static final String ADMIN_SENHA = "Admin@1234";
    protected static final String FUNCIONARIO_EMAIL = "funcionario@padaria.com";
    protected static final String FUNCIONARIO_SENHA = "Func@1234";

    @BeforeEach
    void configurarBanco() {
        pedidoRepository.deleteAll(); // primeiro: FK pedidos → usuarios
        usuarioRepository.deleteAll();
        criarAdmin();
    }

    protected Usuario criarAdmin() {
        var admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail(ADMIN_EMAIL);
        admin.setSenha(passwordEncoder.encode(ADMIN_SENHA));
        admin.setRole(Role.ADMINISTRADOR);
        return usuarioRepository.save(admin);
    }

    protected Usuario criarFuncionario() {
        var funcionario = new Usuario();
        funcionario.setNome("Funcionario Teste");
        funcionario.setEmail(FUNCIONARIO_EMAIL);
        funcionario.setSenha(passwordEncoder.encode(FUNCIONARIO_SENHA));
        funcionario.setRole(Role.FUNCIONARIO);
        return usuarioRepository.save(funcionario);
    }

    protected Usuario criarFuncionarioInativo() {
        var usuario = new Usuario();
        usuario.setNome("Inativo Teste");
        usuario.setEmail("inativo@padaria.com");
        usuario.setSenha(passwordEncoder.encode("Inativo@1234"));
        usuario.setRole(Role.FUNCIONARIO);
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }

    protected String obterTokenAdmin() throws Exception {
        return obterToken(ADMIN_EMAIL, ADMIN_SENHA);
    }

    protected String obterTokenFuncionario() throws Exception {
        criarFuncionario();
        return obterToken(FUNCIONARIO_EMAIL, FUNCIONARIO_SENHA);
    }

    protected String obterToken(String email, String senha) throws Exception {
        var resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return json.path("data").path("token").asText();
    }

    protected String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}
