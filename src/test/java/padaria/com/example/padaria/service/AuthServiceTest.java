package padaria.com.example.padaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import padaria.com.example.padaria.dto.auth.LoginRequestDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.repository.PedidoRepository;
import padaria.com.example.padaria.repository.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthService — Testes de Integração")
class AuthServiceTest {

    @Autowired private IAuthService IAuthService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@padaria.com";
    private static final String ADMIN_SENHA = "Admin@1234";

    @BeforeEach
    void configurarBanco() {
        pedidoRepository.deleteAll(); // primeiro: FK pedidos → usuarios
        usuarioRepository.deleteAll();

        var admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail(ADMIN_EMAIL);
        admin.setSenha(passwordEncoder.encode(ADMIN_SENHA));
        admin.setRole(Role.ADMINISTRADOR);
        usuarioRepository.save(admin);
    }

    @Test
    @DisplayName("Login com credenciais válidas deve retornar LoginResponseDTO com token")
    void login_comCredenciaisValidas_retornaTokenERoleCorretos() {
        var dto = criarLoginRequest(ADMIN_EMAIL, ADMIN_SENHA);

        var resultado = IAuthService.login(dto);

        assertThat(resultado.getToken()).isNotBlank();
        assertThat(resultado.getNome()).isEqualTo("Admin Teste");
        assertThat(resultado.getRole()).isEqualTo("ADMINISTRADOR");
    }

    @Test
    @DisplayName("Login com senha incorreta deve lançar BadCredentialsException")
    void login_comSenhaErrada_lancaExcecao() {
        var dto = criarLoginRequest(ADMIN_EMAIL, "SenhaErrada@1");

        assertThatThrownBy(() -> IAuthService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Login com usuário inativo deve lançar exceção")
    void login_comUsuarioInativo_lancaExcecao() {
        var inativo = new Usuario();
        inativo.setNome("Inativo");
        inativo.setEmail("inativo@padaria.com");
        inativo.setSenha(passwordEncoder.encode("Inativo@1234"));
        inativo.setRole(Role.FUNCIONARIO);
        inativo.setAtivo(false);
        usuarioRepository.save(inativo);

        var dto = criarLoginRequest("inativo@padaria.com", "Inativo@1234");

        assertThatThrownBy(() -> IAuthService.login(dto))
                .isInstanceOf(Exception.class);
    }

    // ==================== HELPERS ====================

    private LoginRequestDTO criarLoginRequest(String email, String senha) {
        try {
            var constructor = LoginRequestDTO.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var dto = constructor.newInstance();

            var emailField = LoginRequestDTO.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(dto, email);

            var senhaField = LoginRequestDTO.class.getDeclaredField("senha");
            senhaField.setAccessible(true);
            senhaField.set(dto, senha);

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar LoginRequestDTO via reflection", e);
        }
    }
}
