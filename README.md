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
# copie .env.example para .env e ajuste os valores
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

- `spring.jpa.hibernate.ddl-auto=create` no Compose de desenvolvimento: recria schema ao subir a aplicacao.
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

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `JWT_SECRET`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `APP_PUBLIC_BASE_URL`

### Exemplo de configuracao

```env
POSTGRES_DB=oficina_db
POSTGRES_USER=oficina_user
POSTGRES_PASSWORD=troque-esta-senha
JWT_SECRET=troque-este-jwt-secret
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
APP_MAIL_FROM=
APP_PUBLIC_BASE_URL=http://localhost:8080
```

## Testes e build

```bash
# testes
.\mvnw.cmd test

# build
.\mvnw.cmd clean package
```

## Kubernetes

Os manifestos simples para K8s estao em [k8s](</C:/Users/arneto/OneDrive - Padtec/Área de Trabalho/Tech Challenge/api/k8s>):

- `configmap.yaml`
- `secret.yaml`
- `postgres.yaml`
- `api.yaml`
- `hpa.yaml`

### Build da imagem

```bash
docker build --target runtime -t api-oficina:latest .
```

### Aplicacao dos manifestos

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/api.yaml
kubectl apply -f k8s/hpa.yaml
```

### Acesso local

```bash
kubectl port-forward service/oficina-api 8080:8080
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`

### Observacoes

- Antes do deploy, ajuste os valores em `k8s/secret.yaml`.
- O `HPA` depende do `metrics-server` instalado no cluster.
- Em ambiente local com `kind`, os manifestos podem ser aplicados normalmente mesmo sem a API de metricas estar disponivel. Nesse caso, o `kubectl top pods` retorna `Metrics API not available` e o HPA aparece com `cpu: <unknown>` e `memory: <unknown>`, sem invalidar os manifests entregues.
- Para manter a solucao simples, o PostgreSQL foi definido com `Deployment` e `Service`. Em ambiente real, o mais adequado seria usar persistencia e, em geral, `StatefulSet`.

## Infraestrutura como Codigo

Os scripts Terraform estao em [infra](</C:/Users/arneto/OneDrive - Padtec/Área de Trabalho/Tech Challenge/api/infra>) e fazem:

- provisionamento do cluster Kubernetes local com `kind`
- build da imagem Docker da API
- carga da imagem Docker da API no cluster `kind`
- instalacao automatica do `metrics-server` para suportar o HPA no `kind`
- aplicacao dos manifests Kubernetes do projeto
- provisionamento do banco PostgreSQL dentro do cluster por meio do manifesto `k8s/postgres.yaml`

### Aplicacao

```bash
cd infra
terraform init
terraform apply
```

### Recursos criados

- cluster Kubernetes local `kind`
- build da imagem Docker da API
- carga da imagem Docker da API no cluster
- instalacao do `metrics-server`
- `ConfigMap`
- `Secret`
- `Deployment` e `Service` do PostgreSQL
- `Deployment` e `Service` da API
- `HorizontalPodAutoscaler`
