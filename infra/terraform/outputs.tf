output "server_public_ip" {
  description = "Public IP of the deployed EC2 server"
  value       = aws_instance.nemologic_server.public_ip
}

output "ssh_connection_string" {
  description = "SSH connection helper command"
  value       = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_instance.nemologic_server.public_ip}"
}

output "backup_bucket_name" {
  description = "Name of the S3 bucket created for DB backups"
  value       = aws_s3_bucket.backup_bucket.id
}
