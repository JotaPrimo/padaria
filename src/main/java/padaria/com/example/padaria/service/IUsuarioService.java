package padaria.com.example.padaria.service;

import padaria.com.example.padaria.dto.usuario.UsuarioRequestDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioResponseDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioUpdateDTO;

import java.util.List;

public interface IUsuarioService {

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO buscarPorId(Long id);

    UsuarioResponseDTO criar(UsuarioRequestDTO dto);

    UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto);

    void inativar(Long id);

    void reativar(Long id);
}
