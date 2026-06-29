# Secondary AWS provider for ACM Certificates in N. Virginia (us-east-1)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}

# --- ACM Certificates ---

resource "aws_acm_certificate" "prod_cert" {
  provider                  = aws.us_east_1
  domain_name               = "rogic.io"
  validation_method         = "DNS"
  subject_alternative_names = ["www.rogic.io"]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "rogic-prod-cert"
  }
}

resource "aws_acm_certificate" "stage_cert" {
  provider          = aws.us_east_1
  domain_name       = "stage.rogic.io"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "rogic-stage-cert"
  }
}

# --- S3 Buckets for Frontend ---

resource "aws_s3_bucket" "frontend_prod_bucket" {
  bucket        = "rogic-frontend-prod-${random_string.suffix.result}"
  force_destroy = true

  tags = {
    Name = "rogic-frontend-prod"
  }
}

resource "aws_s3_bucket" "frontend_stage_bucket" {
  bucket        = "rogic-frontend-stage-${random_string.suffix.result}"
  force_destroy = true

  tags = {
    Name = "rogic-frontend-stage"
  }
}

# --- CloudFront Origin Access Control (OAC) ---

resource "aws_cloudfront_origin_access_control" "oac" {
  name                              = "rogic-cloudfront-oac-${random_string.suffix.result}"
  description                       = "OAC for rogic.io S3 frontend buckets"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# --- S3 Bucket Policies for CloudFront OAC Read Access ---

data "aws_iam_policy_document" "s3_prod_policy" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.frontend_prod_bucket.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.prod_distribution.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "frontend_prod_policy" {
  bucket = aws_s3_bucket.frontend_prod_bucket.id
  policy = data.aws_iam_policy_document.s3_prod_policy.json
}

data "aws_iam_policy_document" "s3_stage_policy" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.frontend_stage_bucket.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.stage_distribution.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "frontend_stage_policy" {
  bucket = aws_s3_bucket.frontend_stage_bucket.id
  policy = data.aws_iam_policy_document.s3_stage_policy.json
}

# --- CloudFront Distributions ---

resource "aws_cloudfront_distribution" "prod_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"
  aliases             = ["rogic.io", "www.rogic.io"]

  # Frontend Origin (S3)
  origin {
    domain_name              = aws_s3_bucket.frontend_prod_bucket.bucket_regional_domain_name
    origin_id                = "S3-Frontend"
    origin_access_control_id = aws_cloudfront_origin_access_control.oac.id
  }

  # Backend Origin (EC2)
  origin {
    domain_name = aws_eip.nemologic_eip.public_dns
    origin_id   = "EC2-Backend"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  # Default Cache Behavior (S3 frontend assets)
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-Frontend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6" # Managed-CachingOptimized
  }

  # Backend API Path Cache Behavior
  ordered_cache_behavior {
    path_pattern           = "/api/*"
    allowed_methods        = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "EC2-Backend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id          = "4135ea2d-6df8-44a3-9896-b6b47ab827c7" # Managed-CachingDisabled
    origin_request_policy_id = "b689b0a8-53d0-40b8-8a1a-d01a7b884a17" # Managed-AllViewerExceptHostHeader
  }

  # Backend Actuator Path Cache Behavior
  ordered_cache_behavior {
    path_pattern           = "/actuator/*"
    allowed_methods        = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "EC2-Backend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id          = "4135ea2d-6df8-44a3-9896-b6b47ab827c7" # Managed-CachingDisabled
    origin_request_policy_id = "b689b0a8-53d0-40b8-8a1a-d01a7b884a17" # Managed-AllViewerExceptHostHeader
  }

  # HTML5 History API Routing fallback support
  custom_error_response {
    error_code            = 404
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 10
  }

  custom_error_response {
    error_code            = 403
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 10
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.prod_cert.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = {
    Name = "rogic-prod-cloudfront"
  }
}

resource "aws_cloudfront_distribution" "stage_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"
  aliases             = ["stage.rogic.io"]

  # Frontend Origin (S3)
  origin {
    domain_name              = aws_s3_bucket.frontend_stage_bucket.bucket_regional_domain_name
    origin_id                = "S3-Frontend"
    origin_access_control_id = aws_cloudfront_origin_access_control.oac.id
  }

  # Backend Origin (EC2)
  origin {
    domain_name = aws_eip.nemologic_staging_eip.public_dns
    origin_id   = "EC2-Backend"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  # Default Cache Behavior (S3 frontend assets)
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-Frontend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6" # Managed-CachingOptimized
  }

  # Backend API Path Cache Behavior
  ordered_cache_behavior {
    path_pattern           = "/api/*"
    allowed_methods        = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "EC2-Backend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id          = "4135ea2d-6df8-44a3-9896-b6b47ab827c7" # Managed-CachingDisabled
    origin_request_policy_id = "b689b0a8-53d0-40b8-8a1a-d01a7b884a17" # Managed-AllViewerExceptHostHeader
  }

  # Backend Actuator Path Cache Behavior
  ordered_cache_behavior {
    path_pattern           = "/actuator/*"
    allowed_methods        = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "EC2-Backend"
    viewer_protocol_policy = "redirect-to-https"

    cache_policy_id          = "4135ea2d-6df8-44a3-9896-b6b47ab827c7" # Managed-CachingDisabled
    origin_request_policy_id = "b689b0a8-53d0-40b8-8a1a-d01a7b884a17" # Managed-AllViewerExceptHostHeader
  }

  # HTML5 History API Routing fallback support
  custom_error_response {
    error_code            = 404
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 10
  }

  custom_error_response {
    error_code            = 403
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 10
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.stage_cert.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = {
    Name = "rogic-stage-cloudfront"
  }
}
