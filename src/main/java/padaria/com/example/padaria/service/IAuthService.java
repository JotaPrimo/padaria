package padaria.com.example.padaria.service;

import padaria.com.example.padaria.dto.auth.LoginRequestDTO;
import padaria.com.example.padaria.dto.auth.LoginResponseDTO;

public interface IAuthService {

    LoginResponseDTO login(LoginRequestDTO dto);
}
