package padaria.com.example.padaria.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.repository.UsuarioRepository;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UsuarioSeeder implements ISeeder {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("pt", "BR"));
    private final Random random = new Random();

    private static final List<String> DOMINIOS = List.of(
            "gmail.com", "hotmail.com", "yahoo.com.br", "outlook.com", "icloud.com"
    );

    @Override
    public void executar(int quantidade) {
        String senhaEncoded = passwordEncoder.encode("Seeder@123");

        for (int i = 0; i < quantidade; i++) {
            var usuario = new Usuario();
            usuario.setNome(faker.name().fullName());
            usuario.setEmail(gerarEmailUnico());
            usuario.setSenha(senhaEncoded);
            usuario.setRole(sortearRole());
            definirAtivo(usuario);
            usuarioRepository.save(usuario);
        }
    }

    private String gerarEmailUnico() {
        String firstName = normalizar(faker.name().firstName());
        String lastName = normalizar(faker.name().lastName());
        String dominio = DOMINIOS.get(random.nextInt(DOMINIOS.size()));
        String base = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String email = base + "@" + dominio;

        if (usuarioRepository.existsByEmail(email)) {
            email = base + random.nextInt(9000) + 1000 + "@" + dominio;
        }

        return email;
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\s+", "");
    }

    private Role sortearRole() {
        return random.nextInt(10) < 2 ? Role.ADMINISTRADOR : Role.FUNCIONARIO;
    }

    private void definirAtivo(Usuario usuario) {
        boolean ativo = random.nextInt(10) > 0;
        usuario.setAtivo(ativo);
        if (!ativo) {
            int diasAtras = random.nextInt(180) + 1;
            usuario.setInativadoEm(LocalDateTime.now().minusDays(diasAtras));
        }
    }
}
