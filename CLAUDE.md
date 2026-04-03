# Contexto do Projeto — Sistema de Gestão de Pedidos (Padaria)

## Visão Geral
API REST para gerenciar encomendas de uma padaria. Substitui anotações em papel por uma plataforma digital centralizada.

**Stack:** Spring Boot 4.0.5 · Java 21 · PostgreSQL · Spring Security + JWT · Lombok · JPA/Hibernate

**Branch principal de desenvolvimento:** `dev`

---

## Como trabalhar neste projeto

- Atuar como arquiteto de software, de forma **iterativa**
- **Fazer perguntas antes de tomar decisões importantes**
- **Só gerar código quando explicitamente solicitado**
- Gerar código **por camada**, aguardando confirmação a cada etapa
- Nunca gerar tudo de uma vez

---

## Decisões Arquiteturais Fixadas

| Decisão | Escolha |
|---|---|
| Pacote base | `padaria.com.example.padaria` |
| Versionamento | `/api/v1/` |
| Resposta padrão | `ApiResponse<T>` com `success`, `message`, `data` |
| Erros de validação | `{ success, message, errors: { campo: [msgs] } }` |
| Autenticação | JWT stateless — `jjwt 0.12.6` |
| Usuários | Cadastrados no PostgreSQL |
| Banco | `padaria_db` · host `localhost:5432` · user/pass `postgres` |
| Injeção de dependência | Interfaces + `private final` + `@RequiredArgsConstructor` — sem `@Autowired` |
| Exclusão de registros | **Proibida** em todos os módulos — apenas inativação/cancelamento |
| DTOs | Agrupados por contexto: `dto/auth/`, `dto/usuario/`, `dto/pedido/` (futuro) |
| Validação de strings | `@Size(min = 5, max = 255)` como padrão |
| Validação de senha | `@SenhaValida` — regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$` |
| Seed do admin | `AdminSeeder` (ApplicationRunner) — email: `admin@padaria.com` / senha: `Admin@1234` |

---

## Estrutura de Pacotes

```
src/main/java/padaria/com/example/padaria/
├── config/
│   ├── AdminSeeder.java          # ApplicationRunner — cria admin na 1ª inicialização
│   └── SecurityConfig.java       # SecurityFilterChain, AuthProvider, PasswordEncoder
├── controller/
│   ├── AuthController.java       # POST /api/v1/auth/login
│   └── UsuarioController.java    # CRUD + inativar/reativar /api/v1/usuarios
├── dto/
│   ├── ApiResponse.java          # Envelope genérico { success, message, data }
│   ├── auth/
│   │   ├── LoginRequestDTO.java
│   │   └── LoginResponseDTO.java
│   └── usuario/
│       ├── UsuarioRequestDTO.java  # Criação — senha obrigatória
│       ├── UsuarioUpdateDTO.java   # Edição — senha opcional
│       └── UsuarioResponseDTO.java # Nunca expõe senha — método estático .de(Usuario)
├── entity/
│   └── Usuario.java              # @PrePersist/@PreUpdate para createdAt/updatedAt
├── enums/
│   └── Role.java                 # FUNCIONARIO, ADMINISTRADOR
├── exception/
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice — trata todos os erros
│   ├── NegocioException.java        # Regra de negócio → HTTP 400
│   └── RecursoNaoEncontradoException.java # → HTTP 404
├── repository/
│   └── UsuarioRepository.java    # findByEmail, existsByEmail
├── security/
│   ├── JwtService.java           # Gerar, assinar e validar tokens (API jjwt 0.12.x)
│   ├── JwtAuthFilter.java        # OncePerRequestFilter — extrai token do header
│   └── UserDetailsServiceImpl.java # loadUserByUsername — rejeita usuário inativo
├── service/
│   ├── AuthService.java          # interface
│   ├── AuthServiceImpl.java
│   ├── UsuarioService.java       # interface
│   └── UsuarioServiceImpl.java
└── validation/
    ├── SenhaValida.java          # @interface (anotação)
    └── SenhaValidator.java       # ConstraintValidator — null delegado ao @NotNull
```

---

## Endpoints Implementados

### Auth
| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/v1/auth/login` | Público |

### Usuários
| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/v1/usuarios` | Autenticado |
| GET | `/api/v1/usuarios/{id}` | ADMIN |
| POST | `/api/v1/usuarios` | ADMIN |
| PUT | `/api/v1/usuarios/{id}` | ADMIN |
| PATCH | `/api/v1/usuarios/{id}/inativar` | ADMIN |
| PATCH | `/api/v1/usuarios/{id}/reativar` | ADMIN |

---

## Entidade Usuario

| Campo | Tipo | Observações |
|---|---|---|
| id | Long | PK, auto-increment |
| nome | String | NOT NULL, max 255 |
| email | String | NOT NULL, UNIQUE |
| senha | String | BCrypt, nunca exposta em DTOs |
| role | Role (enum) | FUNCIONARIO ou ADMINISTRADOR |
| ativo | boolean | default true |
| createdAt | LocalDateTime | preenchido no @PrePersist |
| updatedAt | LocalDateTime | preenchido no @PreUpdate, default null |
| inativadoEm | LocalDateTime | preenchido ao inativar, default null |

---

## Módulo de Pedidos — PRÓXIMO

### Campos da entidade (Dicionário de Dados)
| Campo | Obrigatório | Tipo sugerido |
|---|---|---|
| cliente | SIM | String |
| telefone | SIM | String |
| data_hora_entrega | SIM | LocalDateTime |
| descricao_pedido | SIM | String (texto longo) |
| observacao | NÃO | String (texto longo) |
| status_pedido | SIM | Enum (Pendente, Entregue, Cancelado) |
| valor_pedido | SIM | BigDecimal |
| pagamento_integral | SIM | boolean |
| valor_adiantamento | NÃO | BigDecimal |
| data_cancelamento | NÃO | LocalDate |
| motivo_cancelamento | NÃO | Enum |
| obs_cancelamento | NÃO | String (obrigatória quando motivo = "Outro") |
| cadastrado_por | SIM | FK → Usuario |
| data_ultima_alteracao | SIM | LocalDateTime (automático) |
| alterado_por | SIM | FK → Usuario (automático) |

### Enums pendentes
- `StatusPedido`: PENDENTE, ENTREGUE, CANCELADO
- `MotivoCancelamento`: CANCELADO_PELO_CLIENTE, CANCELADO_PELA_PADARIA, OUTRO

### Requisitos Funcionais
- **RF-001** Listagem com filtros (data entrega, cliente, status, pagamento pendente, usuário) + paginação + ordenação por urgência
- **RF-002** Cadastro — status inicial PENDENTE, cadastrado_por automático
- **RF-003** Edição — campos protegidos: nome do cliente
- **RF-004** Cancelamento — somente ADMIN, registra data, alerta estorno se houver adiantamento
- **RF-005** Sem exclusão física
- **RF-006** Dashboard — pedidos hoje/semana/mês + pagamentos pendentes
- **RF-007** Exportação CSV e PDF com filtros ativos

### Badges de status (frontend/docs)
- Verde → ENTREGUE
- Amarelo → PENDENTE
- Cinza → CANCELADO
- Vermelho → PENDENTE com data_hora_entrega no passado (calculado, sem alterar banco)

---

## Plano de Testes (após API completa)
- Testes de integração com banco real (sem mocks de banco)
- Contexto: aprendizado, então cobrir os fluxos principais de cada módulo

---

## Observações Técnicas
- `ddl-auto=update` — adequado para dev; em produção usar `validate` + Flyway
- `open-in-view=false` — desabilitado para evitar queries durante renderização
- Dialeto PostgreSQL não precisa ser especificado — Hibernate 7 detecta automaticamente
- `DaoAuthenticationProvider` no Spring Security 6+ recebe `UserDetailsService` no construtor
- API `jjwt 0.12.x` usa `Jwts.parser().verifyWith()` — API antiga foi removida
- Roles prefixadas com `ROLE_` para funcionar com `hasRole()` no Spring Security
