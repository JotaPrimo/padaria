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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import padaria.com.example.padaria.dto.ResponseApi;
import padaria.com.example.padaria.dto.pedido.DashboardResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.UsuarioRepository;
import padaria.com.example.padaria.service.IPedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestão de encomendas da padaria. Listagem, cadastro e edição disponíveis para qualquer usuário autenticado. Cancelamento requer perfil ADMINISTRADOR")
public class PedidoController {

    private final IPedidoService IPedidoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(
        summary = "Listar pedidos",
        description = "Retorna os pedidos cadastrados com suporte a filtros e paginação. Ordenação padrão: data de entrega mais urgente primeiro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<Page<PedidoResponseDTO>>> listar(
        @ModelAttribute PedidoFiltroDTO filtro,
        @PageableDefault(sort = "dataHoraEntrega", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseApi.ok("Pedidos listados com sucesso.", IPedidoService.listar(filtro, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar pedido por ID",
        description = "Retorna os dados de um pedido específico pelo seu identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<PedidoResponseDTO>> buscarPorId(
        @Parameter(description = "Identificador único do pedido", example = "1", required = true)
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(ResponseApi.ok("Pedido encontrado.", IPedidoService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar novo pedido",
        description = "Registra uma nova encomenda. O status inicial é definido automaticamente como PENDENTE e o campo 'cadastrado por' é preenchido com o usuário logado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<PedidoResponseDTO>> criar(
        @RequestBody @Valid PedidoRequestDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var usuario = resolverUsuario(userDetails);
        var criado = IPedidoService.criar(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.ok("Pedido criado com sucesso.", criado));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Editar pedido",
        description = "Atualiza as informações editáveis de um pedido existente. O nome do cliente não pode ser alterado. Pedidos cancelados não podem ser editados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<PedidoResponseDTO>> atualizar(
        @Parameter(description = "Identificador único do pedido", example = "1", required = true)
        @PathVariable Long id,
        @RequestBody @Valid PedidoUpdateDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var usuario = resolverUsuario(userDetails);
        return ResponseEntity.ok(ResponseApi.ok("Pedido atualizado com sucesso.", IPedidoService.atualizar(id, dto, usuario)));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
        summary = "Cancelar pedido",
        description = "Registra o cancelamento de um pedido. Requer perfil ADMINISTRADOR. Se houver adiantamento, é necessário confirmar que o estorno foi realizado. O pedido permanece visível na listagem com status CANCELADO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Regra de negócio violada (já cancelado, obs obrigatória, estorno não confirmado)", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado para o ID informado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<Void>> cancelar(
        @Parameter(description = "Identificador único do pedido", example = "1", required = true)
        @PathVariable Long id,
        @RequestBody @Valid PedidoCancelamentoDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var usuario = resolverUsuario(userDetails);
        IPedidoService.cancelar(id, dto, usuario);
        return ResponseEntity.ok(ResponseApi.ok("Pedido cancelado com sucesso."));
    }

    @GetMapping("/dashboard")
    @Operation(
        summary = "Painel de indicadores",
        description = "Retorna os principais indicadores operacionais: pedidos para hoje, esta semana, este mês e com pagamento pendente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Indicadores calculados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<DashboardResponseDTO>> dashboard() {
        return ResponseEntity.ok(ResponseApi.ok("Indicadores calculados com sucesso.", IPedidoService.dashboard()));
    }

    private Usuario resolverUsuario(UserDetails userDetails) {
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }
}
