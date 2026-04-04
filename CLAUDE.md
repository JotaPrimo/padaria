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
| Resposta padrão | `ResponseApi<T>` com `success`, `message`, `data` |
| Erros de validação | `{ success, message, errors: { campo: [msgs] } }` |
| Autenticação | JWT stateless — `jjwt 0.12.6` |
| Usuários | Cadastrados no PostgreSQL |
| Banco | `padaria_db` · host `localhost:5432` · user/pass `postgres` |
| Injeção de dependência | Interfaces `I`-prefixadas + `private final` + `@RequiredArgsConstructor` — sem `@Autowired` |
| Exclusão de registros | **Proibida** em todos os módulos — apenas inativação/cancelamento |
| DTOs | Agrupados por contexto: `dto/auth/`, `dto/usuario/`, `dto/pedido/` |
| Validação de strings | `@Size(min = 5, max = 255)` como padrão |
| Validação de senha | `@SenhaValida` — regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,}$` |
| Seed do admin | `AdminSeeder` (ApplicationRunner) — email: `admin@padaria.com` / senha: `Admin@1234` |
| Naming de interfaces | Prefixo `I` — ex.: `IUsuarioService`, `IPedidoService`, `IAuthService` |
| Utilitários | Pacote `utils/` — ex.: `StringValidator.isNullOrBlank()` |

---

## Estrutura de Pacotes

```
src/main/java/padaria/com/example/padaria/
├── config/
│   ├── AdminSeeder.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UsuarioController.java
│   └── PedidoController.java
├── dto/
│   ├── ResponseApi.java
│   ├── auth/
│   │   ├── LoginRequestDTO.java
│   │   └── LoginResponseDTO.java
│   ├── usuario/
│   │   ├── UsuarioRequestDTO.java
│   │   ├── UsuarioUpdateDTO.java
│   │   └── UsuarioResponseDTO.java
│   └── pedido/
│       ├── PedidoRequestDTO.java
│       ├── PedidoUpdateDTO.java
│       ├── PedidoCancelamentoDTO.java
│       ├── PedidoResponseDTO.java
│       └── DashboardResponseDTO.java
├── entity/
│   ├── Usuario.java
│   └── Pedido.java
├── enums/
│   ├── Role.java
│   ├── StatusPedido.java
│   └── MotivoCancelamento.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── NegocioException.java
│   └── RecursoNaoEncontradoException.java
├── repository/
│   ├── UsuarioRepository.java
│   └── PedidoRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── IAuthService.java
│   ├── AuthServiceImpl.java
│   ├── IUsuarioService.java
│   ├── UsuarioServiceImpl.java
│   ├── IPedidoService.java
│   ├── PedidoServiceImpl.java
│   └── PedidoFilterSpec.java        # JPA Specifications para filtros dinâmicos
├── utils/
│   └── StringValidator.java         # isNullOrBlank()
└── validation/
    ├── SenhaValida.java
    └── SenhaValidator.java
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

### Pedidos
| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/v1/pedidos` | Autenticado |
| GET | `/api/v1/pedidos/{id}` | Autenticado |
| POST | `/api/v1/pedidos` | Autenticado |
| PUT | `/api/v1/pedidos/{id}` | Autenticado |
| PATCH | `/api/v1/pedidos/{id}/cancelar` | ADMIN (`@PreAuthorize`) |
| GET | `/api/v1/pedidos/dashboard` | Autenticado |

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
| createdAt | LocalDateTime | @PrePersist |
| updatedAt | LocalDateTime | @PreUpdate, default null |
| inativadoEm | LocalDateTime | preenchido ao inativar |

---

## Entidade Pedido

| Campo | Tipo | Observações |
|---|---|---|
| id | Long | PK, auto-increment |
| cliente | String | NOT NULL, max 255 — **imutável após criação** |
| telefone | String | NOT NULL, max 20 |
| dataHoraEntrega | LocalDateTime | NOT NULL |
| descricaoPedido | String | NOT NULL, TEXT |
| observacao | String | nullable, TEXT |
| statusPedido | StatusPedido | NOT NULL, default PENDENTE |
| valorPedido | BigDecimal | NOT NULL |
| pagamentoIntegral | boolean | NOT NULL |
| valorAdiantamento | BigDecimal | nullable — obrigatório se `pagamentoIntegral = false` |
| dataCancelamento | LocalDate | nullable |
| motivoCancelamento | MotivoCancelamento | nullable |
| obsCancelamento | String | nullable — obrigatório quando motivo = OUTRO |
| cadastradoPor | ManyToOne → Usuario | NOT NULL, `updatable = false` |
| dataUltimaAlteracao | LocalDateTime | @PrePersist/@PreUpdate automático |
| alteradoPor | ManyToOne → Usuario | NOT NULL, preenchido pelo service |

**Métodos helpers na entidade:** `isPedidoCancelado()`, `isPedidoPendente()`, `isPedidoEntregue()`

**Campo `atrasado`:** calculado no `PedidoResponseDTO.de()` — status PENDENTE + dataHoraEntrega < now. Não armazenado no banco.

---

## Regras de Negócio — Pedidos

- **Criar:** status fixo PENDENTE · `cadastradoPor` = usuário logado · `valorAdiantamento` obrigatório se `!pagamentoIntegral`
- **Editar:** campo `cliente` protegido (ausente no DTO) · pedido CANCELADO não pode ser editado · `valorAdiantamento` obrigatório se `!pagamentoIntegral`
- **Cancelar:** somente ADMIN · motivo OUTRO exige `obsCancelamento` · se há `valorAdiantamento`, `estornoConfirmado` deve ser `true`
- **Exclusão:** proibida — apenas cancelamento

---

## PedidoRepository — Queries

```java
countByDataHoraEntregaBetween(inicio, fim)      // dashboard: hoje/semana/mês
countPagamentoPendente(StatusPedido.CANCELADO)   // dashboard: pagamentos em aberto
```

## PedidoFilterSpec — Filtros Dinâmicos (JPA Specification)

| Parâmetro | Tipo | Comportamento |
|---|---|---|
| `dataEntregaInicio` | LocalDateTime | `>=` |
| `dataEntregaFim` | LocalDateTime | `<=` |
| `cliente` | String | LIKE case-insensitive |
| `statusPedido` | StatusPedido | igual |
| `pagamentoPendente` | Boolean | `pagamentoIntegral = false` AND `status <> CANCELADO` |
| `cadastradoPorId` | Long | FK igual |

---

## Próximo — RF-007: Exportação CSV e PDF

- **Prioridade:** Média (implementar depois de validar o módulo principal)
- **CSV:** `GET /api/v1/pedidos/exportar/csv` · `Content-Type: text/csv` · coluna `total_pagamentos`
- **PDF:** `GET /api/v1/pedidos/exportar/pdf` · `Content-Type: application/pdf`
- Ambos respeitam os mesmos filtros da listagem
- Dependência a adicionar no `pom.xml`: **OpenPDF** (fork LGPL do iText 5)

---

## Testes

### Base
- `IntegrationTestBase` — `@SpringBootTest + MockMvc + @ActiveProfiles("test")`
- `@BeforeEach`: deleta `pedidoRepository` **antes** de `usuarioRepository` (FK constraint)
- Helpers: `criarAdmin()`, `criarFuncionario()`, `obterTokenAdmin()`, `obterTokenFuncionario()`

### Módulo Usuários ✅
- `UsuarioControllerTest` — 14 cenários (integração MockMvc)
- `UsuarioServiceTest` — 10 cenários (integração com banco)

### Módulo Pedidos ✅
- `PedidoControllerTest` — 15 cenários (integração MockMvc)
- `PedidoServiceTest` — 11 cenários (integração com banco)

---

## Observações Técnicas
- `ddl-auto=update` — adequado para dev; em produção usar `validate` + Flyway
- `open-in-view=false` — desabilitado
- `DaoAuthenticationProvider` recebe `UserDetailsService` no construtor (Spring Security 6+)
- API `jjwt 0.12.x` usa `Jwts.parser().verifyWith()`
- Roles prefixadas com `ROLE_` para funcionar com `hasRole()` no Spring Security
- `@EnableMethodSecurity` ativo no `SecurityConfig` — permite `@PreAuthorize` nos controllers
- Usuário logado resolvido nos controllers via `@AuthenticationPrincipal UserDetails` → `usuarioRepository.findByEmail()`
