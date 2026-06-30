output "staging_server_public_ip" {
  description = "Public IP of the staging EC2 server"
  value       = aws_eip.nemologic_staging_eip.public_ip
}

output "staging_instance_id" {
  description = "Instance ID of the staging EC2 server"
  value       = aws_instance.nemologic_staging_server.id
}

output "staging_ssh_connection_string" {
  description = "SSH connection helper command for staging"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.nemologic_staging_eip.public_ip}"
}

output "backup_bucket_name" {
  description = "Name of the S3 bucket created for DB backups"
  value       = aws_s3_bucket.backup_bucket.id
}

output "acm_validation_dns_stage" {
  description = "DNS CNAME records to validate the staging ACM certificate"
  value = [
    for dvo in aws_acm_certificate.stage_cert.domain_validation_options : {
      domain = dvo.domain_name
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      value  = dvo.resource_record_value
    }
  ]
}
