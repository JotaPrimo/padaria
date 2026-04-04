package padaria.com.example.padaria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import padaria.com.example.padaria.dto.auth.LoginRequestDTO;
import padaria.com.example.padaria.dto.auth.LoginResponseDTO;
import padaria.com.example.padaria.entity.Usuario;
import padaria.com.example.padaria.exception.RecursoNaoEncontradoException;
import padaria.com.example.padaria.repository.UsuarioRepository;
import padaria.com.example.padaria.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
        );

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities("ROLE_" + usuario.getRole().name())
                .build();

        String token = jwtService.gerarToken(userDetails);

        return new LoginResponseDTO(token, usuario.getNome(), usuario.getRole().name());
    }
}
