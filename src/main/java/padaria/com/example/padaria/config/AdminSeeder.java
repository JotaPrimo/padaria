package padaria.com.example.padaria.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.repository.UsuarioRepository;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@padaria.com";

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin já existe. Seed ignorado.");
            return;
        }

        var admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(ADMIN_EMAIL);
        admin.setSenha(passwordEncoder.encode("Admin@1234"));
        admin.setRole(Role.ADMINISTRADOR);

        usuarioRepository.save(admin);
        log.info("Admin criado com sucesso. Email: {} | Senha: Admin@1234", ADMIN_EMAIL);
    }
}
