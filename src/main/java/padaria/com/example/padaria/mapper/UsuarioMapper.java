package padaria.com.example.padaria.mapper;

import org.mapstruct.Mapper;
import padaria.com.example.padaria.dto.usuario.UsuarioResponseDTO;
import padaria.com.example.padaria.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}
