output "server_public_ip" {
  description = "Public IP of the deployed EC2 server"
  value       = aws_eip.nemologic_eip.public_ip
}

output "ssh_connection_string" {
  description = "SSH connection helper command"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.nemologic_eip.public_ip}"
}

output "backup_bucket_name" {
  description = "Name of the S3 bucket created for DB backups"
  value       = aws_s3_bucket.backup_bucket.id
}

output "tfstate_bucket_name" {
  description = "Name of the S3 bucket created for Terraform state"
  value       = aws_s3_bucket.tfstate_bucket.id
}

output "tfstate_lock_table" {
  description = "Name of the DynamoDB table created for state locking"
  value       = aws_dynamodb_table.tfstate_lock.name
}

output "acm_validation_dns_prod" {
  description = "DNS CNAME records to validate the production ACM certificate"
  value = [
    for dvo in aws_acm_certificate.prod_cert.domain_validation_options : {
      domain = dvo.domain_name
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      value  = dvo.resource_record_value
    }
  ]
}

output "cloudfront_domain_prod" {
  description = "Domain name of the production CloudFront distribution"
  value       = aws_cloudfront_distribution.prod_distribution.domain_name
}

output "frontend_prod_bucket" {
  description = "Name of the S3 bucket created for production frontend assets"
  value       = aws_s3_bucket.frontend_prod_bucket.id
}
