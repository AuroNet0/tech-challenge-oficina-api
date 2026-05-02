# API Oficina Mecanica

API REST para gestao de oficina: clientes, veiculos, servicos, pecas/estoque e ordens de servico, com autenticacao JWT, RBAC por perfil e acompanhamento publico de OS por token.

## Objetivo

Centralizar o fluxo operacional da oficina e permitir:
- gestao interna com perfis de acesso;
- ciclo completo de ordem de servico;
- consulta/aprovacao de OS pelo cliente sem login (token publico).

## Stack

- Java 21, Spring Boot 4.0.5
- Spring Data JPA + PostgreSQL
- Spring Security + JWT
- Swagger/OpenAPI (`/swagger-ui.html`)
- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Execucao

### Docker Compose
```bash
docker compose up --build
```

### Local (API + PostgreSQL)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`

## Banco e carga inicial

- `spring.jpa.hibernate.ddl-auto=create`: recria schema ao subir a aplicacao.
- `data.sql`: insere 3 usuarios padrao para login:
  - `atendente@oficina.com` (`ATENDENTE`)
  - `mecanico@oficina.com` (`MECANICO`)
  - `gerente@oficina.com` (`GERENTE`)
- Senha dos 3: `123456`

Observacao: para reset fisico completo do banco no Docker (incluindo volume), use `docker compose down -v` antes do `up`.

## Autenticacao

- Login: `POST /auth/login`
- Header para rotas protegidas:
  - `Authorization: Bearer <token>`

## Controle de acesso (RBAC)

Perfis: `ATENDENTE`, `MECANICO`, `GERENTE`.

Regras principais:
- `GERENTE`: acesso total; exclusivo para `/usuarios/**` e operacoes de exclusao.
- `ATENDENTE`: foco em atendimento (clientes, veiculos, criacao OS, envio/aprovacao interna de orcamento).
- `MECANICO`: foco tecnico (itens da OS, status, servicos/pecas/estoque).

## Endpoints publicos (cliente)

- `GET /public/ordens-servico?token={token}`
  - lista status das OS do cliente dono do token.
- `PATCH /public/ordens-servico/{id}/orcamento?token={token}`
  - aprova/reprova orcamento da OS do cliente.

## Regras de negocio de OS

- Itens (servicos/pecas) so podem ser adicionados em OS `RECEBIDA` ou `EM_DIAGNOSTICO`.
- Envio de orcamento: OS deve estar `EM_DIAGNOSTICO`.
- Aprovacao de orcamento: OS deve estar `AGUARDANDO_APROVACAO`.
- Ao aprovar, a OS vai para `EM_EXECUCAO` e ocorre baixa de estoque das pecas.
- Se faltar estoque na aprovacao, a operacao falha com `BusinessException`.

## Variaveis de ambiente principais

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (default atual: `create`)
- `JWT_SECRET`

## Testes e build

```bash
# testes
.\mvnw.cmd test

# build
.\mvnw.cmd clean package
```
