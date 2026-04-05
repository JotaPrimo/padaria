package padaria.com.example.padaria.service;

import padaria.com.example.padaria.dto.pedido.PagamentoRequestDTO;
import padaria.com.example.padaria.dto.pedido.PagamentoResponseDTO;
import padaria.com.example.padaria.entity.Usuario;

public interface IPagamentoService {

    PagamentoResponseDTO registrar(Long pedidoId, PagamentoRequestDTO dto, Usuario usuarioLogado);

    PagamentoResponseDTO invalidar(Long pagamentoId, Usuario usuarioLogado);
}
