variable "region" {
  type    = string
  default = "us-east-1"
}

variable "project" {
  type    = string
  default = "s3-docs"
}

variable "env" {
  type    = string
  default = "dev"
}

variable "docs_prefix" {
  type    = string
  default = "dev-docs/"
}
