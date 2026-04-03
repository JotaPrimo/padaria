package padaria.com.example.padaria.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Dados necessários para autenticação na API")
public class LoginRequestDTO {

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "Deve ser um endereço de email válido.")
    @Schema(
        description = "Email cadastrado do usuário no sistema",
        example = "admin@padaria.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "O campo senha é obrigatório.")
    @Schema(
        description = "Senha do usuário. Deve conter letras maiúsculas, minúsculas, números e caracteres especiais",
        example = "Admin@1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String senha;
}
