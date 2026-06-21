variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "ap-northeast-2"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3a.nano"
}

variable "key_name" {
  description = "Name of the existing SSH key pair in AWS"
  type        = string
  default     = "nemologic-key"
}

variable "alert_email" {
  description = "Email address to receive Gemini API failure alerts"
  type        = string
}

variable "grafana_url" {
  description = "The URL of the Grafana Cloud instance (e.g. https://your-stack.grafana.net)"
  type        = string
  default     = ""
}

variable "grafana_auth" {
  description = "API token or Service Account token for Grafana Cloud authentication"
  type        = string
  sensitive   = true
  default     = ""
}

variable "grafana_sm_url" {
  description = "Synthetic Monitoring API URL (e.g. https://synthetic-monitoring-api-ap-northeast-1.grafana.net)"
  type        = string
  default     = ""
}

variable "grafana_sm_token" {
  description = "The Synthetic Monitoring access token"
  type        = string
  sensitive   = true
  default     = ""
}

variable "grafana_prometheus_datasource_name" {
  description = "The name of the Prometheus datasource in Grafana Cloud"
  type        = string
  default     = "grafanacloud-grandwalrus3189-prom"
}

variable "grafana_cloudwatch_datasource_name" {
  description = "The name of the CloudWatch datasource in Grafana Cloud"
  type        = string
  default     = "CloudWatch"
}




