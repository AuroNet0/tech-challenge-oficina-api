# Tech Challenge Oficina - API

API principal da oficina mecânica desenvolvida em Spring Boot com Java 21. O projeto expõe recursos REST para a operação da oficina, usa autenticação/autorização com JWT e está preparado para execução em Kubernetes no AWS EKS, com imagem publicada no AWS ECR e banco PostgreSQL externo.

## Responsabilidades da API

- Gerenciar clientes.
- Gerenciar veículos vinculados aos clientes.
- Gerenciar serviços oferecidos pela oficina.
- Gerenciar peças e estoque.
- Gerenciar ordens de serviço, incluindo itens, orçamento, status e consulta por cliente.
- Autenticar usuários internos e proteger endpoints com JWT conforme as regras implementadas no `SecurityConfig`.
- Disponibilizar observabilidade por health checks, logs estruturados, correlation ID e eventos/métricas de negócio.

## Tecnologias utilizadas

- Java 21
- Spring Boot 4.0.5
- Spring Security
- Spring Data JPA
- Spring Boot Actuator
- Springdoc OpenAPI/Swagger
- PostgreSQL
- Maven/Maven Wrapper
- Docker
- Docker Compose
- Kubernetes
- AWS EKS
- AWS ECR
- AWS RDS PostgreSQL
- API Gateway
- New Relic
- GitHub Actions
- Testcontainers
- JaCoCo

## Arquitetura e integração

Fluxo principal previsto para o ambiente em nuvem:

```text
Cliente -> API Gateway -> Load Balancer -> API no EKS -> RDS PostgreSQL
```

A API roda como um `Deployment` Kubernetes chamado `oficina-api`, exposto por um `Service` do tipo `LoadBalancer`. O deploy automatizado publica a imagem Docker no ECR, atualiza o cluster EKS e configura a aplicação para acessar o endpoint do RDS PostgreSQL.

A autenticação de cliente por CPF é tratada por um componente serverless separado quando aplicável. Nesta API, o `JwtFilter` reconhece tokens com claim `tipo=CLIENTE`, usa o CPF como principal autenticado e atribui a role `ROLE_CLIENTE` para acesso a endpoints protegidos de cliente.

## Autenticação e perfis

O endpoint de login interno é:

```http
POST /auth/login
```

As rotas protegidas esperam o header:

```http
Authorization: Bearer <token>
```

Perfis e roles considerados no `SecurityConfig`:

| Perfil | Uso principal |
| --- | --- |
| `CLIENTE` | Consulta das próprias ordens de serviço via JWT de cliente. |
| `ATENDENTE` | Cadastro e manutenção de clientes/veículos, criação de ordens de serviço e envio/aprovação de orçamento. |
| `MECANICO` | Consulta operacional, atualização técnica de ordens de serviço, serviços, peças e estoque. |
| `GERENTE` | Acesso administrativo, incluindo usuários e operações de exclusão. |

Também existem endpoints públicos sob `/public/**` para consulta/aprovação por token público, sem login interno.

## Principais endpoints

| Grupo | Responsabilidade |
| --- | --- |
| `/auth` | Login e geração de JWT. |
| `/clientes` | Cadastro, consulta, atualização e exclusão de clientes. |
| `/veiculos` | Cadastro, consulta, atualização e exclusão de veículos. |
| `/servicos` | Cadastro, consulta, atualização e exclusão de serviços. |
| `/pecas` | Cadastro, consulta, atualização, estoque e exclusão de peças. |
| `/ordens-servico` | Criação, consulta, itens, status, orçamento e ordens do cliente autenticado. |
| `/public/ordens-servico` | Consulta pública de ordens por token. |
| `/public/aprovacoes` | Aprovação ou reprovação pública de orçamento por token. |
| `/actuator/health` | Health check geral da aplicação. |

A documentação completa dos contratos REST está disponível via Swagger/OpenAPI.

## Swagger / OpenAPI

Com a aplicação em execução, acesse:

```text
http://localhost:8080/swagger-ui.html
```

Também é permitido pelo Spring Security o caminho `/swagger-ui/**`, incluindo `/swagger-ui/index.html` quando resolvido pelo Springdoc.

O documento OpenAPI fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

A variável `APP_PUBLIC_BASE_URL` define o servidor público exibido no OpenAPI. Em ambiente de nuvem, ela deve apontar para a URL pública exposta para consumo da API, como a URL configurada no API Gateway.

## Variáveis de ambiente

| Variável | Obrigatória | Sensível | Uso |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Sim | Não | URL JDBC do PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | Sim | Não | Usuário do banco. |
| `SPRING_DATASOURCE_PASSWORD` | Sim | Sim | Senha do banco. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Não | Não | Estratégia de DDL do Hibernate (`create`, `update`, etc.). |
| `JWT_SECRET` | Sim | Sim | Chave usada para assinatura/validação dos tokens JWT. |
| `APP_PUBLIC_BASE_URL` | Não | Não | URL pública usada em links e no servidor exibido no OpenAPI. |
| `SPRING_MAIL_USERNAME` | Não | Sim | Usuário SMTP para envio de e-mails. |
| `SPRING_MAIL_PASSWORD` | Não | Sim | Senha SMTP para envio de e-mails. |
| `APP_MAIL_FROM` | Não | Sim | Remetente usado nos e-mails enviados pela aplicação. |
| `NEW_RELIC_ENABLED` | Não | Não | Habilita o agente New Relic no container quando `true`. |
| `NEW_RELIC_APP_NAME` | Não | Não | Nome da aplicação no New Relic. |
| `NEW_RELIC_LICENSE_KEY` | Necessária quando New Relic estiver habilitado | Sim | License key do New Relic. |
| `SPRING_DOCKER_COMPOSE_ENABLED` | Não | Não | Controla a integração do Spring Boot com Docker Compose. |
| `POSTGRES_DB` | Local/Compose | Não | Nome do banco criado pelo container PostgreSQL local. |
| `POSTGRES_USER` | Local/Compose | Não | Usuário criado pelo container PostgreSQL local. |
| `POSTGRES_PASSWORD` | Local/Compose | Sim | Senha criada para o PostgreSQL local. |

Não armazene valores sensíveis no repositório. Use `.env` local, GitHub Environments/Secrets e Secrets do Kubernetes.

## Execução local

### Maven

Para executar localmente com Maven Wrapper, a aplicação espera um PostgreSQL acessível conforme as variáveis `SPRING_DATASOURCE_*`.

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

Por padrão, a aplicação usa `jdbc:postgresql://localhost:5432/oficina_db`, usuário `oficina_user` e senha `oficina_pass` quando as variáveis não são informadas.

### Docker Compose

O projeto inclui `compose.yaml` com PostgreSQL 15 Alpine e a API em modo de desenvolvimento.

```bash
docker compose up --build
```

O Compose usa `.env` quando disponível. O arquivo `.env.example` mostra as variáveis esperadas para execução local.

### Build e testes

```bash
# Windows
.\mvnw.cmd test
.\mvnw.cmd clean package

# Linux/macOS
./mvnw test
./mvnw clean package
```

O projeto possui testes automatizados com Spring Boot Test, Spring Security Test e Testcontainers, além de relatório de cobertura via JaCoCo.

## Banco e carga inicial

O arquivo `src/main/resources/data.sql` cria usuários internos padrão quando ainda não existem:

| E-mail | Perfil |
| --- | --- |
| `atendente@oficina.com` | `ATENDENTE` |
| `mecanico@oficina.com` | `MECANICO` |
| `gerente@oficina.com` | `GERENTE` |

No Compose de desenvolvimento, `SPRING_JPA_HIBERNATE_DDL_AUTO` está configurado como `create`, recriando o schema ao subir a aplicação.

## Health checks

O Actuator expõe apenas endpoints de saúde:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Os manifests Kubernetes usam `/actuator/health/liveness` para `startupProbe` e `livenessProbe`, e `/actuator/health/readiness` para `readinessProbe`.

## Observabilidade

A observabilidade implementada inclui:

- New Relic APM por agente Java no container, habilitado por `NEW_RELIC_ENABLED=true` e `NEW_RELIC_LICENSE_KEY`.
- Integração com a API oficial do New Relic para eventos e métricas customizadas.
- Logs estruturados em JSON via Logback e `logstash-logback-encoder`.
- Correlation ID via header `X-Correlation-ID`; quando ausente, a API gera um UUID e devolve o mesmo header na resposta.
- Inclusão de `correlationId` no MDC dos logs.

Eventos e métricas de negócio implementados:

| Nome | Tipo | Finalidade |
| --- | --- | --- |
| `OrdemServicoCreated` | Evento customizado | Registro de criação de ordem de serviço. |
| `OrdemServicoStatusChanged` | Evento customizado | Registro de alteração de status da ordem. |
| `OrdemServicoStageDuration` | Evento customizado e métrica de tempo | Duração de etapas da ordem de serviço. |
| `ExternalIntegrationError` | Evento customizado | Falhas em integrações externas, como envio de e-mail. |
| `Custom/Business/OrdemServico/Created` | Métrica | Contagem de ordens de serviço criadas. |
| `Custom/Business/OrdemServico/StageDuration/{STATUS}` | Métrica | Tempo gasto por status/etapa da ordem. |

## Kubernetes

Os manifests Kubernetes estão em `k8s/`:

| Arquivo | Função |
| --- | --- |
| `api.yaml` | `Deployment` e `Service` `LoadBalancer` da API. |
| `configmap.yaml` | Configurações não sensíveis da aplicação. |
| `secret.example.yaml` | Exemplo de Secret esperado pela aplicação. |
| `secret.yaml` | Secret local do repositório. Não deve conter credenciais reais versionadas. |
| `hpa.yaml` | Horizontal Pod Autoscaler para CPU e memória. |

O container expõe a porta `8080`, usa probes do Actuator e define requests/limits de CPU e memória. A imagem base de runtime é `eclipse-temurin:21-jre-alpine`.

## CI/CD

O workflow `.github/workflows/ci-cd.yml` roda em:

- `push` para `homolog` e `master`;
- `pull_request` para `homolog` e `master`;
- execução manual via `workflow_dispatch`.

Job `CI`:

- checkout do repositório;
- configuração do Java 21 com cache Maven;
- execução de testes com `./mvnw -B test`;
- build do pacote com `./mvnw -B clean package -DskipTests`;
- build da imagem Docker com target `runtime`.

Job `Deploy`:

- executa somente em `push` para `homolog` ou `master`, nunca em Pull Request;
- usa o environment `homolog` para branch `homolog` e `production` para branch `master`;
- autentica na AWS por OIDC usando `AWS_DEPLOY_ROLE_ARN`;
- publica imagem no AWS ECR com tags do commit e do ambiente (`homolog` ou `production`);
- atualiza o kubeconfig do cluster EKS `tech-challenge-oficina`;
- obtém o endpoint do RDS `tech-challenge-oficina-postgres`;
- cria/atualiza ConfigMap e Secret no Kubernetes a partir de variáveis e secrets do GitHub;
- aplica `k8s/api.yaml` e `k8s/hpa.yaml`;
- atualiza a imagem do deployment `oficina-api`;
- aguarda o rollout e coleta diagnóstico em caso de falha.

## Deploy

O deploy é automatizado pelo GitHub Actions. Alterações integradas nas branches `homolog` ou `master` passam por testes, build Maven, build Docker, publicação no ECR e atualização do deployment no EKS.

Não há necessidade de comandos manuais de deploy no fluxo principal. Credenciais e segredos devem ser fornecidos por GitHub Environments/Secrets e Kubernetes Secrets.

## Estratégia de branches

- `homolog`: branch de homologação.
- `master`: branch de produção.
- Alterações devem ser propostas via Pull Request.
- Branches protegidas devem seguir o fluxo definido para o projeto, garantindo revisão e execução da pipeline antes da integração.

## Segurança

- Não armazenar secrets no repositório.
- Usar GitHub Environments/Secrets para CI/CD.
- Usar Kubernetes Secrets para credenciais em runtime.
- Proteger endpoints com JWT.
- Aplicar RBAC da aplicação conforme roles do `SecurityConfig`.
- Manter credenciais de banco, SMTP, JWT e New Relic fora de arquivos versionados.

## Regras principais de ordem de serviço

- Itens de serviço e peças são adicionados à ordem de serviço conforme regras da camada de serviço.
- O orçamento pode ser enviado para aprovação.
- A aprovação do orçamento altera o fluxo da ordem e realiza baixa de estoque quando aplicável.
- Falta de estoque ou transições inválidas geram exceções de negócio.

## Relação com demais repositórios

- `tech-challenge-oficina-auth`
- `tech-challenge-oficina-k8s-infra`
- `tech-challenge-oficina-database-infra`
