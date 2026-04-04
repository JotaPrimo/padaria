package padaria.com.example.padaria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import padaria.com.example.padaria.dto.usuario.UsuarioRequestDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioUpdateDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UsuarioService — Testes de Integração")
class UsuarioServiceTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void configurarBanco() {
        usuarioRepository.deleteAll();
    }

    // ==================== CRIAR ====================

    @Test
    @DisplayName("Criar usuário com dados válidos deve persistir e retornar DTO")
    void criar_comDadosValidos_persisteERetornaDTO() {
        var dto = criarRequestDTO("João Silva", "joao@padaria.com", "Senha@1234", Role.FUNCIONARIO);

        var resultado = usuarioService.criar(dto);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("João Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@padaria.com");
        assertThat(resultado.getRole()).isEqualTo(Role.FUNCIONARIO);
        assertThat(resultado.isAtivo()).isTrue();
        assertThat(resultado.getCreatedAt()).isNotNull();
        assertThat(resultado.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Criar usuário com email já cadastrado deve lançar NegocioException")
    void criar_comEmailDuplicado_lancaNegocioException() {
        criarUsuarioNoBanco("joao@padaria.com");
        var dto = criarRequestDTO("João Outro", "joao@padaria.com", "Senha@1234", Role.FUNCIONARIO);

        assertThatThrownBy(() -> usuarioService.criar(dto))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("email");
    }

    // ==================== BUSCAR POR ID ====================

    @Test
    @DisplayName("Buscar por ID existente deve retornar o usuário")
    void buscarPorId_idExistente_retornaUsuario() {
        var usuario = criarUsuarioNoBanco("maria@padaria.com");

        var resultado = usuarioService.buscarPorId(usuario.getId());

        assertThat(resultado.getId()).isEqualTo(usuario.getId());
        assertThat(resultado.getEmail()).isEqualTo("maria@padaria.com");
    }

    @Test
    @DisplayName("Buscar por ID inexistente deve lançar RecursoNaoEncontradoException")
    void buscarPorId_idInexistente_lancaRecursoNaoEncontrado() {
        assertThatThrownBy(() -> usuarioService.buscarPorId(99999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ==================== ATUALIZAR ====================

    @Test
    @DisplayName("Atualizar usuário com dados válidos deve persistir as alterações")
    void atualizar_comDadosValidos_persisteAlteracoes() {
        var usuario = criarUsuarioNoBanco("ana@padaria.com");
        var dto = criarUpdateDTO("Ana Atualizada", "ana@padaria.com", null, Role.ADMINISTRADOR);

        var resultado = usuarioService.atualizar(usuario.getId(), dto);

        assertThat(resultado.getNome()).isEqualTo("Ana Atualizada");
        assertThat(resultado.getRole()).isEqualTo(Role.ADMINISTRADOR);

        var doBanco = usuarioRepository.findById(usuario.getId()).get();
        assertThat(doBanco.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Atualizar usuário sem enviar senha não deve alterar a senha existente")
    void atualizar_semSenha_mantemSenhaAnterior() {
        var usuario = criarUsuarioNoBanco("carlos@padaria.com");
        var senhaAntes = usuarioRepository.findById(usuario.getId()).get().getSenha();
        var dto = criarUpdateDTO("Carlos Novo", "carlos@padaria.com", null, Role.FUNCIONARIO);

        usuarioService.atualizar(usuario.getId(), dto);

        var senhaDepois = usuarioRepository.findById(usuario.getId()).get().getSenha();
        assertThat(senhaDepois).isEqualTo(senhaAntes);
    }

    @Test
    @DisplayName("Atualizar com email já em uso por outro usuário deve lançar NegocioException")
    void atualizar_comEmailJaEmUso_lancaNegocioException() {
        criarUsuarioNoBanco("existente@padaria.com");
        var outro = criarUsuarioNoBanco("outro@padaria.com");
        var dto = criarUpdateDTO("Outro", "existente@padaria.com", null, Role.FUNCIONARIO);

        assertThatThrownBy(() -> usuarioService.atualizar(outro.getId(), dto))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("email");
    }

    // ==================== INATIVAR ====================

    @Test
    @DisplayName("Inativar usuário ativo deve setar ativo=false e preencher inativadoEm")
    void inativar_usuarioAtivo_setaInativadoEmEAtivo() {
        var usuario = criarUsuarioNoBanco("pedro@padaria.com");

        usuarioService.inativar(usuario.getId());

        var atualizado = usuarioRepository.findById(usuario.getId()).get();
        assertThat(atualizado.isAtivo()).isFalse();
        assertThat(atualizado.getInativadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Inativar usuário já inativo deve lançar NegocioException")
    void inativar_usuarioJaInativo_lancaNegocioException() {
        var inativo = criarUsuarioInativoNoBanco();

        assertThatThrownBy(() -> usuarioService.inativar(inativo.getId()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("inativo");
    }

    // ==================== REATIVAR ====================

    @Test
    @DisplayName("Reativar usuário inativo deve setar ativo=true")
    void reativar_usuarioInativo_setaAtivoTrue() {
        var inativo = criarUsuarioInativoNoBanco();

        usuarioService.reativar(inativo.getId());

        var atualizado = usuarioRepository.findById(inativo.getId()).get();
        assertThat(atualizado.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Reativar usuário já ativo deve lançar NegocioException")
    void reativar_usuarioJaAtivo_lancaNegocioException() {
        var usuario = criarUsuarioNoBanco("lucia@padaria.com");

        assertThatThrownBy(() -> usuarioService.reativar(usuario.getId()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ativo");
    }

    // ==================== HELPERS ====================

    private Usuario criarUsuarioNoBanco(String email) {
        var usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("Senha@1234"));
        usuario.setRole(Role.FUNCIONARIO);
        return usuarioRepository.save(usuario);
    }

    private Usuario criarUsuarioInativoNoBanco() {
        var usuario = new Usuario();
        usuario.setNome("Inativo Teste");
        usuario.setEmail("inativo@padaria.com");
        usuario.setSenha(passwordEncoder.encode("Inativo@1234"));
        usuario.setRole(Role.FUNCIONARIO);
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }

    private UsuarioRequestDTO criarRequestDTO(String nome, String email, String senha, Role role) {
        return criarDTO(UsuarioRequestDTO.class,
                new String[]{"nome", "email", "senha", "role"},
                new Object[]{nome, email, senha, role});
    }

    private UsuarioUpdateDTO criarUpdateDTO(String nome, String email, String senha, Role role) {
        return criarDTO(UsuarioUpdateDTO.class,
                new String[]{"nome", "email", "senha", "role"},
                new Object[]{nome, email, senha, role});
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
