package padaria.com.example.padaria.seeder;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import padaria.com.example.padaria.dto.ResponseApi;

@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
@Profile("!prod")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Dev / Seeders", description = "Endpoints de desenvolvimento para popular o banco com dados fictícios. Disponíveis apenas em perfis não-produtivos.")
public class SeederController {

    private final UsuarioSeeder usuarioSeeder;
    private final PedidoSeeder pedidoSeeder;

    @PostMapping("/seed/usuarios")
    public ResponseEntity<ResponseApi<String>> seedUsuarios(
            @RequestParam(defaultValue = "10") int quantidade) {
        usuarioSeeder.executar(quantidade);
        return ResponseEntity.ok(ResponseApi.ok(quantidade + " usuário(s) criado(s) com sucesso."));
    }

    @PostMapping("/seed/pedidos")
    public ResponseEntity<ResponseApi<String>> seedPedidos(
            @RequestParam(defaultValue = "20") int quantidade) {
        pedidoSeeder.executar(quantidade);
        return ResponseEntity.ok(ResponseApi.ok(quantidade + " pedido(s) criado(s) com sucesso."));
    }

    @PostMapping("/seed/todos")
    public ResponseEntity<ResponseApi<String>> seedTodos(
            @RequestParam(defaultValue = "10") int usuarios,
            @RequestParam(defaultValue = "20") int pedidos) {
        usuarioSeeder.executar(usuarios);
        pedidoSeeder.executar(pedidos);
        return ResponseEntity.ok(ResponseApi.ok(
                usuarios + " usuário(s) e " + pedidos + " pedido(s) criados com sucesso."));
    }
}
