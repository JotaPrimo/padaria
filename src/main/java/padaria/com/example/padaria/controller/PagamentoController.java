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
import org.springframework.web.bind.annotation.*;
import padaria.com.example.padaria.dto.ResponseApi;
import padaria.com.example.padaria.dto.pagamento.PagamentoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.UsuarioRepository;
import padaria.com.example.padaria.service.IPagamentoService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pagamentos")
@Tag(name = "Pagamentos", description = "Registro e gestão de pagamentos vinculados a pedidos")
public class PagamentoController {

    private final IPagamentoService pagamentoService;
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
    public ResponseEntity<ResponseApi<Page<PagamentoResponseDTO>>> listar(
            @ModelAttribute PagamentoFiltroDTO filtro,
            @PageableDefault(sort = "dataHoraEntrega", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ResponseApi.ok("Pedidos listados com sucesso.", IPagamentoService.listar(filtro, pageable)));
    }

    @PostMapping("/{pedidoId}/")
    @Operation(
        summary = "Registrar pagamento",
        description = "Registra um novo pagamento para o pedido. A soma dos pagamentos válidos não pode ultrapassar o valor total do pedido. Pedidos cancelados não aceitam novos pagamentos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pagamento registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Valor inválido ou soma excede o total do pedido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<PagamentoResponseDTO>> registrar(
        @Parameter(description = "Identificador do pedido", example = "1", required = true)
        @PathVariable Long pedidoId,
        @RequestBody @Valid PagamentoRequestDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var usuario = resolverUsuario(userDetails);
        var pagamento = pagamentoService.registrar(pedidoId, dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.ok("Pagamento registrado com sucesso.", pagamento));
    }

    @PatchMapping("/{id}/invalidar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
        summary = "Invalidar pagamento",
        description = "Marca um pagamento como inválido, removendo-o do total pago. O valor fica registrado no histórico mas não é contabilizado. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento invalidado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Pagamento já está invalidado", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Token não informado ou inválido", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "Sem permissão — requer perfil ADMINISTRADOR", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<ResponseApi<PagamentoResponseDTO>> invalidar(
        @Parameter(description = "Identificador do pagamento", example = "1", required = true)
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var usuario = resolverUsuario(userDetails);
        var pagamento = pagamentoService.invalidar(id, usuario);
        return ResponseEntity.ok(ResponseApi.ok("Pagamento invalidado com sucesso.", pagamento));
    }

    private Usuario resolverUsuario(UserDetails userDetails) {
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }
}
