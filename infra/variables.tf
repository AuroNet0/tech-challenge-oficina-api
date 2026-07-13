variable "cluster_name" {
  description = "Nome do cluster local criado com kind."
  type        = string
  default     = "oficina-cluster"
}

variable "kubernetes_node_image" {
  description = "Imagem dos nodes do kind."
  type        = string
  default     = "kindest/node:v1.33.1"
}

variable "api_image_name" {
  description = "Nome da imagem Docker da API carregada no cluster kind."
  type        = string
  default     = "api-oficina:latest"
}

variable "metrics_server_manifest_url" {
  description = "Manifesto do metrics-server aplicado no cluster local."
  type        = string
  default     = "https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
}
