package padaria.com.example.padaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import padaria.com.example.padaria.dto.pagamento.PagamentoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoResponseDTO;
import padaria.com.example.padaria.dto.pedido.PedidoFiltroDTO;
import padaria.com.example.padaria.dto.pedido.PedidoResponseDTO;
import padaria.com.example.padaria.entity.Usuario;

public interface IPagamentoService {
    Page<PagamentoResponseDTO> listar(PagamentoFiltroDTO filtro, Pageable pageable);


    PagamentoResponseDTO registrar(Long pedidoId, PagamentoRequestDTO dto, Usuario usuarioLogado);

    PagamentoResponseDTO invalidar(Long pagamentoId, Usuario usuarioLogado);
}
