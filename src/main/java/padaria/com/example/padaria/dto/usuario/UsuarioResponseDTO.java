package padaria.com.example.padaria.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.enums.Role;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private Role role;
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime inativadoEm;

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.isAtivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt(),
                usuario.getInativadoEm()
        );
    }
}
