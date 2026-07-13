output "cluster_name" {
  description = "Nome do cluster local criado."
  value       = var.cluster_name
}

output "kube_context" {
  description = "Contexto kubectl esperado apos o apply."
  value       = "kind-${var.cluster_name}"
}

output "resources_summary" {
  description = "Resumo dos recursos aplicados pelo Terraform."
  value = [
    "Cluster Kubernetes local com kind",
    "Build da imagem Docker da API",
    "Carga da imagem Docker no cluster kind",
    "Instalacao do metrics-server para HPA",
    "ConfigMap da aplicacao",
    "Secret da aplicacao",
    "Deployment e Service do PostgreSQL",
    "Deployment e Service da API",
    "Horizontal Pod Autoscaler da API"
  ]
}
