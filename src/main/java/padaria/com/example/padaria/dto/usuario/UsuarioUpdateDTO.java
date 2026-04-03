package padaria.com.example.padaria.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.validation.SenhaValida;

@Getter
@Schema(description = "Dados para atualização de um usuário existente")
public class UsuarioUpdateDTO {

    @NotBlank(message = "O campo nome é obrigatório.")
    @Size(min = 5, max = 255, message = "O campo nome deve ter entre 5 e 255 caracteres.")
    @Schema(
        description = "Nome completo do usuário",
        example = "João da Silva",
        minLength = 5,
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nome;

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "Deve ser um endereço de email válido.")
    @Size(min = 5, max = 255, message = "O campo email deve ter entre 5 e 255 caracteres.")
    @Schema(
        description = "Endereço de email do usuário. Deve ser único no sistema",
        example = "joao.silva@padaria.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @SenhaValida
    @Schema(
        description = "Nova senha de acesso. Campo opcional — envie apenas se desejar alterar a senha. Regras: mínimo 8 caracteres, ao menos 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial (@#$%^&+=!)",
        example = "NovaSenha@2024",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String senha;

    @NotNull(message = "O campo role é obrigatório.")
    @Schema(
        description = "Perfil de acesso do usuário",
        example = "FUNCIONARIO",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"FUNCIONARIO", "ADMINISTRADOR"}
    )
    private Role role;
}
