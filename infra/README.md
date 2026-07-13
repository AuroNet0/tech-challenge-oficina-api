# Infra com Terraform

Esta pasta contem os scripts Terraform do projeto, atendendo ao requisito de Infraestrutura como Codigo.

## O que o Terraform cria

- Cluster Kubernetes local com `kind`
- Build da imagem Docker da API
- Carga da imagem Docker da API no cluster `kind`
- Instalacao do `metrics-server` ajustado para `kind`
- Aplicacao dos manifests Kubernetes do projeto:
  - `ConfigMap`
  - `Secret`
  - `Deployment` e `Service` do PostgreSQL
  - `Deployment` e `Service` da API
  - `HorizontalPodAutoscaler`

## Pre-requisitos

- Docker
- `kind`
- `kubectl`
- Terraform
- Docker Desktop em execucao

## Como aplicar

No diretorio `infra`:

```bash
terraform init
terraform apply
```

Ao final, o Terraform:

1. cria o cluster local `kind`
2. faz o build da imagem Docker da API
3. carrega a imagem no cluster `kind`
4. instala o `metrics-server` com ajuste para `kind`
5. aplica os manifests em `../k8s`
6. reinicia o deployment da API para garantir uso da imagem atual

## Como validar

```bash
kubectl get pods
kubectl get svc
kubectl get hpa
kubectl top pods
```

## Como destruir

```bash
terraform destroy
```

O destroy remove o cluster `kind`. Os recursos Kubernetes sao removidos junto com ele.
