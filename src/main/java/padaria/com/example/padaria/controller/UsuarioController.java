package padaria.com.example.padaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import padaria.com.example.padaria.dto.ResponseApi;
import padaria.com.example.padaria.dto.usuario.UsuarioRequestDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioResponseDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioUpdateDTO;
import padaria.com.example.padaria.service.IUsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gestão de usuários do sistema. Listagem disponível para qualquer usuário autenticado. Demais operações requerem perfil ADMINISTRADOR")
public class UsuarioController {

    private final IUsuarioService IUsuarioService;

    @GetMapping
    @Operation(
        summary = "Listar todos os usuários",
        description = "Retorna a lista completa de usuários cadastrados no sistema, incluindo ativos e inativos. Acessível por qualquer usuário autenticado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<List<UsuarioResponseDTO>>> listarTodos() {
        return ResponseEntity.ok(ResponseApi.ok("Usuários listados com sucesso.", IUsuarioService.listarTodos()));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os dados de um usuário específico pelo seu identificador. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<UsuarioResponseDTO>> buscarPorId(
        @Parameter(description = "Identificador único do usuário", example = "1", required = true)
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(ResponseApi.ok("Usuário encontrado.", IUsuarioService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar novo usuário",
        description = "Cria um novo usuário no sistema com perfil FUNCIONARIO ou ADMINISTRADOR. O email deve ser único. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<UsuarioResponseDTO>> criar(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = IUsuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.ok("Usuário criado com sucesso.", criado));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário existente. A senha é opcional — se não informada, permanece inalterada. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já em uso por outro usuário", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<UsuarioResponseDTO>> atualizar(
        @Parameter(description = "Identificador único do usuário", example = "1", required = true)
        @PathVariable Long id,
        @RequestBody @Valid UsuarioUpdateDTO dto
    ) {
        return ResponseEntity.ok(ResponseApi.ok("Usuário atualizado com sucesso.", IUsuarioService.atualizar(id, dto)));
    }

    @PatchMapping("/{id}/inativar")
    @Operation(
        summary = "Inativar usuário",
        description = "Desativa o acesso de um usuário ao sistema. O usuário não é excluído — seus dados são preservados e ele não conseguirá mais realizar login. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário inativado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Usuário já está inativo", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<Void>> inativar(
        @Parameter(description = "Identificador único do usuário", example = "1", required = true)
        @PathVariable Long id
    ) {
        IUsuarioService.inativar(id);
        return ResponseEntity.ok(ResponseApi.ok("Usuário inativado com sucesso."));
    }

    @PatchMapping("/{id}/reativar")
    @Operation(
        summary = "Reativar usuário",
        description = "Restaura o acesso de um usuário previamente inativado. O usuário voltará a conseguir realizar login no sistema. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário reativado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Usuário já está ativo", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<Void>> reativar(
        @Parameter(description = "Identificador único do usuário", example = "1", required = true)
        @PathVariable Long id
    ) {
        IUsuarioService.reativar(id);
        return ResponseEntity.ok(ResponseApi.ok("Usuário reativado com sucesso."));
    }
}
