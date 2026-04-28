# API Oficina Mecanica

API REST para gestao de uma oficina mecanica, cobrindo cadastro de clientes, veiculos, pecas/insumos, servicos e ordens de servico, com autenticacao JWT.

## Objetivos do Projeto

- Centralizar o fluxo operacional da oficina em uma API unica.
- Permitir abertura, acompanhamento e fechamento de ordens de servico.
- Controlar estoque de pecas/insumos e itens aplicados em ordens.
- Proteger rotas de negocio com autenticacao baseada em token JWT.
- Disponibilizar documentacao interativa via Swagger/OpenAPI.

## Principais Funcionalidades

- Autenticacao:
  - Login com email/senha e emissao de token JWT.
- Usuarios:
  - CRUD de usuarios administrativos.
- Clientes:
  - CRUD de clientes (PF/PJ com validacoes de CPF/CNPJ).
- Veiculos:
  - CRUD de veiculos e listagem por cliente.
- Servicos:
  - CRUD de servicos e listagem de ativos.
- Pecas e Insumos:
  - CRUD, atualizacao de estoque, listagem de ativos e estoque baixo.
- Ordens de Servico:
  - Criacao de OS.
  - Inclusao de servicos e pecas.
  - Alteracao de status.
  - Aprovacao/reprovacao de orcamento.
  - Listagens por cliente, veiculo e status.

## Especificacoes Tecnicas

- Linguagem: Java 21
- Framework: Spring Boot 4.0.5
- Persistencia: Spring Data JPA + PostgreSQL
- Seguranca: Spring Security + JWT (jjwt 0.12.6)
- Documentacao API: springdoc-openapi (Swagger UI)
- Build: Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Testes: JUnit + Spring Test + Testcontainers + JaCoCo
- Porta padrao da aplicacao: `8080`
- Banco padrao: PostgreSQL em `localhost:5432` (database `oficina_db`)

## Tecnologias Utilizadas

- Java 21
- Spring Boot (Web MVC, Data JPA, Security, Validation)
- PostgreSQL
- Docker / Docker Compose
- OpenAPI/Swagger
- Lombok
- Testcontainers
- JaCoCo

## Estrutura Resumida do Projeto

`src/main/java/com/oficina/api`:

- `controller`: endpoints REST.
- `service`: regras de negocio.
- `repository`: acesso ao banco (JPA).
- `model`: entidades e enums.
- `dto`: contratos de entrada/saida.
- `security`: JWT filter, util e configuracao de seguranca.
- `config`: configuracoes gerais (ex.: OpenAPI).
- `validation`, `exception`, `mapper`, `util`: suporte.

## Configuracao de Ambiente

A aplicacao usa as variaveis abaixo (com defaults):

- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/oficina_db`)
- `SPRING_DATASOURCE_USERNAME` (default: `oficina_user`)
- `SPRING_DATASOURCE_PASSWORD` (default: `oficina_pass`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (default: `update`)
- `JWT_SECRET` (default: `minha-chave-secreta`)

Arquivo de referencia: `src/main/resources/application.yaml`.

## Como Executar Localmente (Simples)

### Opcao 1 - Com Docker Compose (recomendado para subir tudo junto)

Requisitos:

- Docker e Docker Compose instalados.

Passos:

1. Na raiz do projeto, execute:
   - `docker compose up --build`
2. Aguarde os containers:
   - `oficina_db` (PostgreSQL)
   - `oficina_app` (API)
3. Acesse:
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Opcao 2 - API local + PostgreSQL local

Requisitos:

- Java 21
- PostgreSQL 15+ (ou compativel)

Passos:

1. Crie um banco PostgreSQL chamado `oficina_db`.
2. Configure usuario/senha conforme `application.yaml` (ou exporte variaveis de ambiente).
3. Execute:
   - Windows: `.\mvnw.cmd spring-boot:run`
   - Linux/macOS: `./mvnw spring-boot:run`
4. Acesse:
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

## Build e Testes

- Rodar testes:
  - Windows: `.\mvnw.cmd test`
  - Linux/macOS: `./mvnw test`
- Gerar pacote:
  - Windows: `.\mvnw.cmd clean package`
  - Linux/macOS: `./mvnw clean package`
- Relatorio de cobertura (JaCoCo):
  - `target/site/jacoco/index.html`

## Autenticacao e Uso Basico

### 1) Criar usuario (se banco estiver vazio)

Rota protegida: `POST /usuarios`.

Observacao: para ambiente novo, normalmente e comum criar um usuario inicial diretamente no banco ou adaptar bootstrap de dados. Se ja existir usuario, siga para o login.

### 2) Login

- Endpoint publico: `POST /auth/login`
- Retorna token JWT no formato Bearer.

Exemplo de body:

```json
{
  "email": "admin@oficina.com",
  "senha": "123456"
}
```

### 3) Chamar endpoints protegidos

Enviar header:

`Authorization: Bearer <seu_token>`

## Endpoints Principais

- Auth:
  - `POST /auth/login`
- Usuarios:
  - `POST /usuarios`
  - `GET /usuarios`
  - `GET /usuarios/{id}`
  - `PUT /usuarios/{id}`
  - `DELETE /usuarios/{id}`
- Clientes:
  - `POST /clientes`
  - `GET /clientes`
  - `GET /clientes/{id}`
  - `PUT /clientes/{id}`
  - `DELETE /clientes/{id}`
- Veiculos:
  - `POST /veiculos`
  - `GET /veiculos`
  - `GET /veiculos/{id}`
  - `GET /veiculos/cliente/{clienteId}`
  - `PUT /veiculos/{id}`
  - `DELETE /veiculos/{id}`
- Servicos:
  - `POST /servicos`
  - `GET /servicos`
  - `GET /servicos/ativos`
  - `GET /servicos/{id}`
  - `PUT /servicos/{id}`
  - `DELETE /servicos/{id}`
- Pecas:
  - `POST /pecas`
  - `GET /pecas`
  - `GET /pecas/ativas`
  - `GET /pecas/estoque-baixo?quantidade={n}`
  - `GET /pecas/{id}`
  - `PUT /pecas/{id}`
  - `PATCH /pecas/{id}/estoque`
  - `DELETE /pecas/{id}`
- Ordens de Servico:
  - `POST /ordens-servico`
  - `GET /ordens-servico`
  - `GET /ordens-servico/{id}`
  - `GET /ordens-servico/cliente/{clienteId}`
  - `GET /ordens-servico/veiculo/{veiculoId}`
  - `GET /ordens-servico/status/{status}`
  - `POST /ordens-servico/{id}/servicos`
  - `POST /ordens-servico/{id}/pecas`
  - `PATCH /ordens-servico/{id}/status`
  - `PATCH /ordens-servico/{id}/orcamento`

## Regras e Convencoes Relevantes

- Quase todas as rotas exigem autenticacao JWT; excecoes:
  - `/auth/**`
  - `/swagger-ui/**`
  - `/swagger-ui.html`
  - `/v3/api-docs/**`
- Erros de validacao, negocio e recurso nao encontrado sao tratados por handler global.
- `ddl-auto=update` facilita desenvolvimento local; para producao, considerar estrategia de migracoes versionadas.

## Documentacao da API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Containerizacao

Arquivos:

- `Dockerfile`: build multi-stage da aplicacao Java.
- `compose.yaml`: sobe app + PostgreSQL com healthcheck.

Comando util:

- `docker compose up --build -d`
