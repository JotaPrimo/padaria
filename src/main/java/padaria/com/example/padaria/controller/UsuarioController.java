package padaria.com.example.padaria.controller;

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
import padaria.com.example.padaria.dto.ApiResponse;
import padaria.com.example.padaria.dto.usuario.UsuarioRequestDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioResponseDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioUpdateDTO;
import padaria.com.example.padaria.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Usuários listados com sucesso.", usuarioService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuário encontrado.", usuarioService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> criar(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Usuário criado com sucesso.", criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioUpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Usuário atualizado com sucesso.", usuarioService.atualizar(id, dto)));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable Long id) {
        usuarioService.inativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuário inativado com sucesso."));
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<ApiResponse<Void>> reativar(@PathVariable Long id) {
        usuarioService.reativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuário reativado com sucesso."));
    }
}
