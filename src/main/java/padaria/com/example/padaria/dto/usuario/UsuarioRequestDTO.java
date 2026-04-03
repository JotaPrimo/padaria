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
@Schema(description = "Dados para cadastro de um novo usuário no sistema")
public class UsuarioRequestDTO {

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
        description = "Endereço de email do usuário. Utilizado como login no sistema. Deve ser único",
        example = "joao.silva@padaria.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "O campo senha é obrigatório.")
    @SenhaValida
    @Schema(
        description = "Senha de acesso. Regras: mínimo 8 caracteres, ao menos 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial (@#$%^&+=!)",
        example = "Senha@1234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String senha;

    @NotNull(message = "O campo role é obrigatório.")
    @Schema(
        description = "Perfil de acesso do usuário. FUNCIONARIO pode visualizar e cadastrar pedidos. ADMINISTRADOR tem acesso total, incluindo cancelamentos e gestão de usuários",
        example = "FUNCIONARIO",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"FUNCIONARIO", "ADMINISTRADOR"}
    )
    private Role role;
}
