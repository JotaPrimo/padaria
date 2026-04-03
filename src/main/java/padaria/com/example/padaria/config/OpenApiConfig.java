package padaria.com.example.padaria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestão de Pedidos — Padaria")
                        .description("""
                                API REST para gerenciamento de encomendas de uma padaria.

                                ## Autenticação
                                Esta API utiliza **JWT (Bearer Token)**. Para acessar os endpoints protegidos:
                                1. Realize o login em `POST /api/v1/auth/login`
                                2. Copie o token retornado no campo `data.token`
                                3. Clique em **Authorize** (canto superior direito) e cole o token

                                ## Perfis de acesso
                                - **Público** — endpoints de autenticação
                                - **Autenticado** — qualquer usuário com token válido
                                - **ADMIN** — somente usuários com role `ADMINISTRADOR`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Padaria")
                                .email("admin@padaria.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe o token JWT obtido no endpoint de login. Não inclua o prefixo 'Bearer '.")));
    }
}
