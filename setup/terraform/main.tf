terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = ">= 4.52.0"
    }
  }
}
provider "aws" {
  region = "eu-central-1"
}


resource "aws_s3_bucket" "shipment-list-demo-bucket" {
  bucket = "shipment-list-demo-bucket"
}


resource "aws_s3_bucket_acl" "shipment-list-demo-bucket-acl" {
  bucket = aws_s3_bucket.shipment-list-demo-bucket.id
  acl = "private"
}

resource "aws_dynamodb_table" "shipment" {
  name           = "shipment"
  read_capacity  = 10
  write_capacity = 5

  attribute {
    name = "shipmentId"
    type = "S"
  }
  hash_key = "shipmentId"
  server_side_encryption {
        enabled = true
      }

      stream_enabled = true
      stream_view_type = "NEW_AND_OLD_IMAGES"
}


resource "aws_dynamodb_table_item" "shipment" {
  for_each = local.tf_data
  table_name = aws_dynamodb_table.shipment.name
  hash_key   = "shipmentId"
  item = jsonencode(each.value)
}

