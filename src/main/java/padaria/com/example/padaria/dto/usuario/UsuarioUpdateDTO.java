package padaria.com.example.padaria.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import padaria.com.example.padaria.enums.Role;
import padaria.com.example.padaria.validation.SenhaValida;

@Getter
public class UsuarioUpdateDTO {

    @NotBlank(message = "O campo nome é obrigatório.")
    @Size(min = 5, max = 255, message = "O campo nome deve ter entre 5 e 255 caracteres.")
    private String nome;

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "Deve ser um endereço de email válido.")
    @Size(min = 5, max = 255, message = "O campo email deve ter entre 5 e 255 caracteres.")
    private String email;

    @SenhaValida
    private String senha;

    @NotNull(message = "O campo role é obrigatório.")
    private Role role;
}
