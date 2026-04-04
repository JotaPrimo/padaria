package padaria.com.example.padaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import padaria.com.example.padaria.dto.pedido.DashboardResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoCancelamentoDTO;
import padaria.com.example.padaria.dto.pedido.PedidoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoUpdateDTO;
import padaria.com.example.padaria.entity.Usuario;

public interface IPedidoService {

    Page<PedidoResponseDTO> listar(PedidoFiltroDTO filtro, Pageable pageable);

    PedidoResponseDTO buscarPorId(Long id);

    PedidoResponseDTO criar(PedidoRequestDTO dto, Usuario usuarioLogado);

    PedidoResponseDTO atualizar(Long id, PedidoUpdateDTO dto, Usuario usuarioLogado);

    void cancelar(Long id, PedidoCancelamentoDTO dto, Usuario usuarioLogado);

    DashboardResponseDTO dashboard();
}
