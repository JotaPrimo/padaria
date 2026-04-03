package padaria.com.example.padaria.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Resposta retornada após autenticação bem-sucedida")
public class LoginResponseDTO {

    @Schema(description = "Token JWT a ser utilizado nas requisições autenticadas. Envie no header: Authorization: Bearer {token}", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Nome completo do usuário autenticado", example = "Administrador")
    private String nome;

    @Schema(description = "Perfil de acesso do usuário autenticado", example = "ADMINISTRADOR", allowableValues = {"FUNCIONARIO", "ADMINISTRADOR"})
    private String role;
}
