output "server_public_ip" {
  description = "Public IP of the deployed EC2 server"
  value       = aws_eip.nemologic_eip.public_ip
}

output "staging_server_public_ip" {
  description = "Public IP of the staging EC2 server"
  value       = aws_eip.nemologic_staging_eip.public_ip
}

output "staging_instance_id" {
  description = "Instance ID of the staging EC2 server"
  value       = aws_instance.nemologic_staging_server.id
}

output "ssh_connection_string" {
  description = "SSH connection helper command"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.nemologic_eip.public_ip}"
}

output "staging_ssh_connection_string" {
  description = "SSH connection helper command for staging"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.nemologic_staging_eip.public_ip}"
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
