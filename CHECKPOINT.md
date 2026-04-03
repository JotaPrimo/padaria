# Guia de Checkpoints do Projeto

Este arquivo explica como usar tags Git para salvar e restaurar pontos estáveis do projeto,
mantendo o código e o contexto do CLAUDE.md sempre sincronizados.

---

## O que é um Checkpoint?

Um checkpoint é uma **tag Git** criada em um commit específico que representa um estado
completo e funcional do projeto. Diferente de um hash de commit (ex: `d8cf3a5`), a tag
tem um nome legível (ex: `modulo-auth-completo`) que comunica exatamente o que foi
entregue naquele ponto.

---

## Checkpoints disponíveis

| Tag | O que está incluído |
|---|---|
| `modulo-auth-completo` | Módulo de autenticação e usuários 100% funcional + CLAUDE.md |

---

## Como criar um novo Checkpoint

Faça isso sempre que terminar um módulo ou entrega significativa.

```bash
# 1. Certifique-se de que todos os arquivos estão commitados
git status
# Deve retornar: "nothing to commit, working tree clean"

# 2. Atualize o CLAUDE.md com o estado atual do projeto
#    (módulos concluídos, próximos passos, decisões novas)

# 3. Commite o CLAUDE.md atualizado
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md checkpoint for <nome-do-modulo>"

# 4. Crie a tag no commit atual (HEAD)
git tag <nome-da-tag>
# Exemplo:
git tag modulo-pedidos-completo

# 5. Envie a tag para o repositório remoto
git push origin <nome-da-tag>
# Exemplo:
git push origin modulo-pedidos-completo
```

---

## Como voltar para um Checkpoint

Use isso quando quiser restaurar o projeto para um estado anterior.

> ⚠️ ATENÇÃO: o comando abaixo é destrutivo e irreversível localmente.
> Todo trabalho não commitado será perdido.
> Se tiver dúvida, crie uma branch de backup antes de prosseguir.

```bash
# (Opcional) Criar backup da branch atual antes de resetar
git checkout -b backup/antes-do-reset
git checkout dev

# 1. Ver todos os checkpoints disponíveis
git tag
# Saída esperada:
# modulo-auth-completo
# modulo-pedidos-completo
# ...

# 2. Ver o que está incluído em um checkpoint específico
git show modulo-auth-completo --stat
# Mostra o commit, a mensagem e os arquivos alterados naquele ponto

# 3. Resetar para o checkpoint desejado
#    Isso move o HEAD e o working directory para o estado exato da tag
git reset --hard modulo-auth-completo
# O que acontece:
#   - Todos os arquivos voltam ao estado daquele commit
#   - O CLAUDE.md fica sincronizado com o código
#   - Commits feitos APÓS a tag são removidos do histórico local

# 4. (Opcional) Se quiser sincronizar o remoto também
#    Use apenas se tiver certeza — reescreve o histórico remoto
git push origin dev --force
```

---

## Como inspecionar um Checkpoint sem resetar

Se quiser apenas ver o estado do código em um checkpoint sem alterar nada:

```bash
# Cria uma branch temporária a partir da tag para explorar sem risco
git checkout -b inspecao/modulo-auth modulo-auth-completo

# Explore o código normalmente...

# Quando terminar, volte para dev e delete a branch temporária
git checkout dev
git branch -D inspecao/modulo-auth
```

---

## Boas práticas

- **Sempre atualize o CLAUDE.md antes de criar uma tag** — ele é o contrato entre o código e o contexto
- **Use nomes descritivos para as tags** — `modulo-pedidos-completo` é melhor que `v2`
- **Crie um checkpoint ao final de cada módulo** — nunca no meio de uma implementação
- **Nunca force-push na branch `main`** — apenas em `dev` e com cautela
