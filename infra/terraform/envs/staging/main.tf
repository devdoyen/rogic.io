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
    key            = "staging/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "nemologic-tfstate-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}

# --- VPC Configuration (Staging Isolated Network) ---
resource "aws_vpc" "nemologic_staging_vpc" {
  cidr_block           = "10.1.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "nemologic-staging-vpc"
  }
}

# Subnet Configuration
resource "aws_subnet" "nemologic_staging_subnet" {
  vpc_id                  = aws_vpc.nemologic_staging_vpc.id
  cidr_block              = "10.1.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = {
    Name = "nemologic-staging-subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "nemologic_staging_igw" {
  vpc_id = aws_vpc.nemologic_staging_vpc.id

  tags = {
    Name = "nemologic-staging-igw"
  }
}

# Route Table
resource "aws_route_table" "nemologic_staging_rt" {
  vpc_id = aws_vpc.nemologic_staging_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.nemologic_staging_igw.id
  }

  tags = {
    Name = "nemologic-staging-rt"
  }
}

# Route Table Association
resource "aws_route_table_association" "nemologic_staging_rta" {
  subnet_id      = aws_subnet.nemologic_staging_subnet.id
  route_table_id = aws_route_table.nemologic_staging_rt.id
}

# Security Group Configuration
resource "aws_security_group" "nemologic_staging_sg" {
  name        = "nemologic-staging-sg"
  description = "Allow HTTP/HTTPS Nginx access"
  vpc_id      = aws_vpc.nemologic_staging_vpc.id

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
    Name = "nemologic-staging-sg"
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
  bucket        = "nemologic-db-backup-stage-${random_string.suffix.result}"
  force_destroy = true

  tags = {
    Name = "nemologic-staging-db-backup"
  }
}

# IAM Role & Instance Profile for EC2
resource "aws_iam_role" "nemologic_staging_ec2_role" {
  name = "nemologic-staging-ec2-role"

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
    Name = "nemologic-staging-ec2-role"
  }
}

# Policy for S3 Backup Access
resource "aws_iam_policy" "s3_backup_policy" {
  name        = "nemologic-staging-s3-backup-policy"
  description = "Allow staging EC2 instance to upload DB backups to S3"

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
  role       = aws_iam_role.nemologic_staging_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "s3_backup_attachment" {
  role       = aws_iam_role.nemologic_staging_ec2_role.name
  policy_arn = aws_iam_policy.s3_backup_policy.arn
}

resource "aws_iam_role_policy_attachment" "cloudwatch_log_attachment" {
  role       = aws_iam_role.nemologic_staging_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy" # Use standard AWS policy for cloudwatch logging
}

resource "aws_iam_instance_profile" "nemologic_staging_ec2_profile" {
  name = "nemologic-staging-ec2-instance-profile"
  role = aws_iam_role.nemologic_staging_ec2_role.name
}

# Staging EC2 Instance
resource "aws_instance" "nemologic_staging_server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.nemologic_staging_subnet.id
  vpc_security_group_ids = [aws_security_group.nemologic_staging_sg.id]
  key_name               = var.key_name
  iam_instance_profile   = aws_iam_instance_profile.nemologic_staging_ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = {
    Name = "nemologic-staging-server"
  }
}

# Elastic IP (EIP) Allocation & Association
resource "aws_eip" "nemologic_staging_eip" {
  instance = aws_instance.nemologic_staging_server.id
  domain   = "vpc"

  tags = {
    Name = "nemologic-staging-eip"
  }
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "nemologic_staging_log_group" {
  name              = "/aws/ec2/nemologic-staging"
  retention_in_days = 7

  tags = {
    Name = "nemologic-staging-log-group"
  }
}

# SNS Topic for System Alerts
resource "aws_sns_topic" "nemologic_staging_alerts" {
  name = "nemologic-staging-system-alerts"
}

# SNS Email Subscription
resource "aws_sns_topic_subscription" "email_subscription" {
  topic_arn = aws_sns_topic.nemologic_staging_alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}

# CloudWatch Metric Alarm for Staging EC2 Status Check Failed
resource "aws_cloudwatch_metric_alarm" "staging_ec2_status_check_alarm" {
  alarm_name          = "nemologic-staging-ec2-status-check-alarm"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  alarm_description   = "This alarm triggers when the Staging EC2 instance status check fails."
  alarm_actions       = [aws_sns_topic.nemologic_staging_alerts.arn]

  dimensions = {
    InstanceId = aws_instance.nemologic_staging_server.id
  }

  tags = {
    Name = "nemologic-staging-ec2-status-check-alarm"
  }
}

# --- S3 Backup Bucket Lifecycle Rule ---
resource "aws_s3_bucket_lifecycle_configuration" "backup_lifecycle" {
  bucket = aws_s3_bucket.backup_bucket.id

  rule {
    id     = "cleanup-old-backups"
    status = "Enabled"

    filter {
      prefix = "postgres/"
    }

    expiration {
      days = 30
    }
  }
}
