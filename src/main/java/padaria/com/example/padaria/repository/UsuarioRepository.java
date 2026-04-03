package padaria.com.example.padaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import padaria.com.example.padaria.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
