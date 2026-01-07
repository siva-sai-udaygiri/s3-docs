data "aws_caller_identity" "current" {}

resource "random_id" "suffix" {
  byte_length = 3
}

locals {
  base = "${var.project}-${var.env}-${data.aws_caller_identity.current.account_id}-${var.region}-${random_id.suffix.hex}"
}

resource "aws_s3_bucket" "docs" {
  bucket = "${local.base}-docs"
}

resource "aws_s3_bucket" "artifacts" {
  bucket = "${local.base}-artifacts"
}

# Security hardening: private + block public + encryption + versioning
resource "aws_s3_bucket_public_access_block" "docs" {
  bucket                  = aws_s3_bucket.docs.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "docs" {
  bucket = aws_s3_bucket.docs.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "docs" {
  bucket = aws_s3_bucket.docs.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
  }
}

# Enforce TLS (production-grade)
data "aws_iam_policy_document" "docs_tls_only" {
  statement {
    sid     = "DenyInsecureTransport"
    effect  = "Deny"
    actions = ["s3:*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    resources = [
      aws_s3_bucket.docs.arn,
      "${aws_s3_bucket.docs.arn}/*"
    ]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "docs" {
  bucket = aws_s3_bucket.docs.id
  policy = data.aws_iam_policy_document.docs_tls_only.json
}

# App policy (least privilege for the docs prefix)
resource "aws_iam_policy" "app_s3_policy" {
  name = "${var.project}-${var.env}-app-s3-${random_id.suffix.hex}"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ListBucketPrefix"
        Effect = "Allow"
        Action = ["s3:ListBucket"]
        Resource = aws_s3_bucket.docs.arn
        Condition = {
          StringLike = {
            "s3:prefix" = ["${var.docs_prefix}*"]
          }
        }
      },
      {
        Sid    = "ObjectRW"
        Effect = "Allow"
        Action = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
        Resource = "${aws_s3_bucket.docs.arn}/${var.docs_prefix}*"
      },
      {
        Sid    = "HeadBucket"
        Effect = "Allow"
        Action = ["s3:HeadBucket"]
        Resource = aws_s3_bucket.docs.arn
      }
    ]
  })
}
