terraform {
  required_version = ">= 1.0.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "nemologic-tfstate-ey12fmas"
    key            = "terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "nemologic-tfstate-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}

# VPC Configuration
resource "aws_vpc" "nemologic_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "nemologic-vpc"
  }
}

# Subnet Configuration
resource "aws_subnet" "nemologic_subnet" {
  vpc_id                  = aws_vpc.nemologic_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = {
    Name = "nemologic-subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "nemologic_igw" {
  vpc_id = aws_vpc.nemologic_vpc.id

  tags = {
    Name = "nemologic-igw"
  }
}

# Route Table
resource "aws_route_table" "nemologic_rt" {
  vpc_id = aws_vpc.nemologic_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.nemologic_igw.id
  }

  tags = {
    Name = "nemologic-rt"
  }
}

# Route Table Association
resource "aws_route_table_association" "nemologic_rta" {
  subnet_id      = aws_subnet.nemologic_subnet.id
  route_table_id = aws_route_table.nemologic_rt.id
}

# Security Group Configuration
resource "aws_security_group" "nemologic_sg" {
  name        = "nemologic-sg"
  description = "Allow SSH, HTTP backend, and HTTP frontend access"
  vpc_id      = aws_vpc.nemologic_vpc.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Frontend HTTP"
    from_port   = 5173
    to_port     = 5173
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Backend HTTP"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP Nginx"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS Nginx"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Name = "nemologic-sg"
  }
}

# AMI Search for Ubuntu 22.04 LTS
data "aws_ami" "ubuntu" {
  most_recent = true
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  owners = ["099720109477"] # Canonical
}

# Random suffix for unique S3 bucket name
resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

# S3 Bucket for Database Backups
resource "aws_s3_bucket" "backup_bucket" {
  bucket        = "nemologic-db-backup-${random_string.suffix.result}"
  force_destroy = true

  tags = {
    Name = "nemologic-db-backup"
  }
}

# IAM Role & Instance Profile for EC2
resource "aws_iam_role" "nemologic_ec2_role" {
  name = "nemologic-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "nemologic-ec2-role"
  }
}

# Policy for S3 Backup Access
resource "aws_iam_policy" "s3_backup_policy" {
  name        = "nemologic-s3-backup-policy"
  description = "Allow EC2 instance to upload DB backups to S3"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.backup_bucket.arn,
          "${aws_s3_bucket.backup_bucket.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.nemologic_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "s3_backup_attachment" {
  role       = aws_iam_role.nemologic_ec2_role.name
  policy_arn = aws_iam_policy.s3_backup_policy.arn
}

resource "aws_iam_instance_profile" "nemologic_ec2_profile" {
  name = "nemologic-ec2-instance-profile"
  role = aws_iam_role.nemologic_ec2_role.name
}

# EC2 Instance
resource "aws_instance" "nemologic_server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.nemologic_subnet.id
  vpc_security_group_ids = [aws_security_group.nemologic_sg.id]
  key_name               = var.key_name
  iam_instance_profile   = aws_iam_instance_profile.nemologic_ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = {
    Name = "nemologic-server"
  }
}

# S3 Bucket for Terraform State
resource "aws_s3_bucket" "tfstate_bucket" {
  bucket        = "nemologic-tfstate-${random_string.suffix.result}"
  force_destroy = false # Prevent accidental deletion of state

  tags = {
    Name = "nemologic-tfstate"
  }
}

resource "aws_s3_bucket_versioning" "tfstate_versioning" {
  bucket = aws_s3_bucket.tfstate_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

# DynamoDB Table for State Locking
resource "aws_dynamodb_table" "tfstate_lock" {
  name         = "nemologic-tfstate-lock"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name = "nemologic-tfstate-lock"
  }
}

# Elastic IP (EIP) Allocation & Association
resource "aws_eip" "nemologic_eip" {
  instance = aws_instance.nemologic_server.id
  domain   = "vpc"

  tags = {
    Name = "nemologic-eip"
  }
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "nemologic_log_group" {
  name              = "/aws/ec2/nemologic"
  retention_in_days = 7

  tags = {
    Name = "nemologic-log-group"
  }
}

# Policy for CloudWatch Logs Access
resource "aws_iam_policy" "cloudwatch_log_policy" {
  name        = "nemologic-cloudwatch-log-policy"
  description = "Allow EC2 instance to send logs to CloudWatch"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:DescribeLogGroups"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cloudwatch_log_attachment" {
  role       = aws_iam_role.nemologic_ec2_role.name
  policy_arn = aws_iam_policy.cloudwatch_log_policy.arn
}

# SNS Topic for System Alerts
resource "aws_sns_topic" "nemologic_alerts" {
  name = "nemologic-system-alerts"
}

# SNS Email Subscription
resource "aws_sns_topic_subscription" "email_subscription" {
  topic_arn = aws_sns_topic.nemologic_alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}

# CloudWatch Log Metric Filter for General Server Errors
resource "aws_cloudwatch_log_metric_filter" "server_error_filter" {
  name           = "ServerErrorFilter"
  pattern        = "?ERROR ?\" 500 \" ?\"Internal Server Error\""
  log_group_name = aws_cloudwatch_log_group.nemologic_log_group.name

  metric_transformation {
    name      = "ServerErrorCount"
    namespace = "Nemologic/System"
    value     = "1"
  }
}

# CloudWatch Metric Alarm for General Server Errors
resource "aws_cloudwatch_metric_alarm" "server_error_alarm" {
  alarm_name          = "nemologic-server-error-alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "ServerErrorCount"
  namespace           = "Nemologic/System"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  alarm_description   = "This alarm triggers when general server errors (ERROR level logs or HTTP 500 status codes) are detected in the logs."
  alarm_actions       = [aws_sns_topic.nemologic_alerts.arn]

  tags = {
    Name = "nemologic-server-error-alarm"
  }
}


