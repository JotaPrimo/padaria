package padaria.com.example.padaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import padaria.com.example.padaria.dto.ResponseApi;
import padaria.com.example.padaria.dto.auth.LoginRequestDTO;
import padaria.com.example.padaria.dto.auth.LoginResponseDTO;
import padaria.com.example.padaria.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoint público para autenticação de usuários e obtenção do token JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
        summary = "Realizar login",
        description = """
            Autentica o usuário com email e senha e retorna um token JWT.

            O token retornado deve ser enviado no header `Authorization` de todas as requisições protegidas:
            ```
            Authorization: Bearer {token}
            ```
            O token expira em **24 horas**.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. Token JWT retornado no campo `data.token`",
            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos — email ou senha não informados ou formato inválido",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Credenciais incorretas — email ou senha inválidos",
            content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(ResponseApi.ok("Login realizado com sucesso.", response));
    }
}
