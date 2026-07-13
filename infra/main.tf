terraform {
  required_version = ">= 1.5.0"

  required_providers {
    null = {
      source  = "hashicorp/null"
      version = "~> 3.2"
    }
  }
}

locals {
  project_root = "${path.module}/.."

  source_files = sort(distinct(concat(
    ["Dockerfile", "pom.xml"],
    [for file in fileset(local.project_root, "src/main/java/**") : file],
    [for file in fileset(local.project_root, "src/main/resources/**") : file]
  )))

  source_hash = sha256(join("", [
    for file in local.source_files : filesha256("${local.project_root}/${file}")
  ]))
}

resource "null_resource" "kind_cluster" {
  triggers = {
    cluster_name       = var.cluster_name
    kubernetes_version = var.kubernetes_node_image
    kind_config_hash   = filesha256("${path.module}/kind-config.yaml")
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      $clusters = kind get clusters 2>$null
      if ($clusters -notcontains "${var.cluster_name}") {
        kind create cluster --name "${var.cluster_name}" --image "${var.kubernetes_node_image}" --config "${path.module}/kind-config.yaml"
      } else {
        Write-Host "Cluster ${var.cluster_name} ja existe."
      }
    EOT
  }

  provisioner "local-exec" {
    when        = destroy
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      $clusters = kind get clusters 2>$null
      if ($clusters -contains "${self.triggers.cluster_name}") {
        kind delete cluster --name "${self.triggers.cluster_name}"
      } else {
        Write-Host "Cluster ${self.triggers.cluster_name} nao encontrado."
      }
    EOT
  }
}

resource "null_resource" "api_image_build" {
  triggers = {
    source_hash = local.source_hash
    image_name  = var.api_image_name
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      docker build --target runtime -t "${var.api_image_name}" "${local.project_root}"
    EOT
  }
}

resource "null_resource" "api_image_load" {
  depends_on = [null_resource.kind_cluster, null_resource.api_image_build]

  triggers = {
    cluster_name = var.cluster_name
    source_hash  = local.source_hash
    image_name   = var.api_image_name
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      kind load docker-image "${var.api_image_name}" --name "${var.cluster_name}"
    EOT
  }
}

resource "null_resource" "metrics_server" {
  depends_on = [null_resource.kind_cluster]

  triggers = {
    cluster_name           = var.cluster_name
    metrics_server_version = var.metrics_server_manifest_url
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      kubectl apply -f "${var.metrics_server_manifest_url}"
      kubectl patch deployment metrics-server -n kube-system --type='strategic' -p '{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"metrics-server\",\"args\":[\"--cert-dir=/tmp\",\"--secure-port=10250\",\"--kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname\",\"--kubelet-use-node-status-port\",\"--metric-resolution=15s\",\"--kubelet-insecure-tls\"]}]}}}}'
      kubectl rollout status deployment/metrics-server -n kube-system --timeout=180s
    EOT
  }
}

resource "null_resource" "kubernetes_manifests" {
  depends_on = [null_resource.kind_cluster, null_resource.api_image_load, null_resource.metrics_server]

  triggers = {
    configmap_hash = filesha256("${path.module}/../k8s/configmap.yaml")
    secret_hash    = filesha256("${path.module}/../k8s/secret.yaml")
    postgres_hash  = filesha256("${path.module}/../k8s/postgres.yaml")
    api_hash       = filesha256("${path.module}/../k8s/api.yaml")
    hpa_hash       = filesha256("${path.module}/../k8s/hpa.yaml")
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      kubectl cluster-info
      kubectl apply -f "${path.module}/../k8s/configmap.yaml"
      kubectl apply -f "${path.module}/../k8s/secret.yaml"
      kubectl apply -f "${path.module}/../k8s/postgres.yaml"
      kubectl apply -f "${path.module}/../k8s/api.yaml"
      kubectl apply -f "${path.module}/../k8s/hpa.yaml"
    EOT
  }
}

resource "null_resource" "api_rollout_restart" {
  depends_on = [null_resource.kubernetes_manifests]

  triggers = {
    source_hash = local.source_hash
    image_name  = var.api_image_name
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      kubectl rollout restart deployment/oficina-api
      kubectl rollout status deployment/oficina-api --timeout=180s
    EOT
  }
}
