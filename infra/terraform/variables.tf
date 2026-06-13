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
  default     = "dev@example.com"
}

