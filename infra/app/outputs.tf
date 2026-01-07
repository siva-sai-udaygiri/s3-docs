output "docs_bucket_name" { value = aws_s3_bucket.docs.bucket }
output "artifacts_bucket_name" { value = aws_s3_bucket.artifacts.bucket }
output "docs_prefix" { value = var.docs_prefix }
output "app_policy_arn" { value = aws_iam_policy.app_s3_policy.arn }
