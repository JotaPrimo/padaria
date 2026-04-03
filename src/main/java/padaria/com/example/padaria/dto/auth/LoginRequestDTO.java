package padaria.com.example.padaria.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDTO {

    @NotBlank(message = "O campo email é obrigatório.")
    @Email(message = "Deve ser um endereço de email válido.")
    private String email;

    @NotBlank(message = "O campo senha é obrigatório.")
    private String senha;
}
