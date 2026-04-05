package padaria.com.example.padaria.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import padaria.com.example.padaria.enums.Role;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Dados do usuário retornados pela API")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    private String nome;

    @Schema(description = "Endereço de email do usuário", example = "joao.silva@padaria.com")
    private String email;

    @Schema(description = "Perfil de acesso do usuário", example = "FUNCIONARIO")
    private Role role;

    @Schema(description = "Indica se o usuário está ativo e pode realizar login no sistema", example = "true")
    private boolean ativo;

    @Schema(description = "Data e hora em que o usuário foi cadastrado no sistema", example = "2025-04-03T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização dos dados do usuário. Nulo se nunca foi editado", example = "2025-04-10T14:30:00", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "Data e hora em que o usuário foi inativado. Nulo se o usuário está ativo", example = "null", nullable = true)
    private LocalDateTime inativadoEm;

}
