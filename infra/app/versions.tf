terraform {
  required_version = ">= 1.5.0"

  backend "s3" {
    # Fill these after bootstrap (see commands below)
    # bucket = "..."
    # key    = "s3-docs/app/terraform.tfstate"
    # region = "us-east-1"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

provider "aws" {
  region = var.region
}
