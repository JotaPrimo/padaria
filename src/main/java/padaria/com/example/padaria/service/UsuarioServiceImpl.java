package padaria.com.example.padaria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import padaria.com.example.padaria.dto.usuario.UsuarioRequestDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioResponseDTO;
import padaria.com.example.padaria.dto.usuario.UsuarioUpdateDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.exception.NegocioException;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::de)
                .toList();
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.de(buscarOuLancar(id));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        validarEmailJaCadastrado(dto);

        var usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(dto.getRole());

        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        var usuario = buscarOuLancar(id);

        validarEmailNaoDuplicado(dto, usuario);

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setRole(dto.getRole());

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void inativar(Long id) {
        var usuario = buscarOuLancar(id);

        if (!usuario.isAtivo()) {
            throw new NegocioException("O usuário já está inativo.");
        }

        usuario.setAtivo(false);
        usuario.setInativadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void reativar(Long id) {
        var usuario = buscarOuLancar(id);

        garantirUsuarioInativo(usuario);

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarOuLancar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void validarEmailJaCadastrado(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new NegocioException("Já existe um usuário cadastrado com o email informado.");
        }
    }

    private void validarEmailNaoDuplicado(UsuarioUpdateDTO dto, Usuario usuario) {
        if (!usuario.getEmail().equals(dto.getEmail())
                && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new NegocioException("Já existe um usuário cadastrado com o email informado.");
        }
    }

    private static void garantirUsuarioInativo(Usuario usuario) {
        if (usuario.isAtivo()) {
            throw new NegocioException("O usuário já está ativo.");
        }
    }
}
