
# declares the provider it will be using (AWS) and the minimum
# version of the provider required to run the script
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.52.0"
    }
  }
}
provider "aws" {
  region = "eu-central-1"
}

# S3 bucket, named "shipment-picture-bucket", which is set to be destroyed even if it
# has non-empty contents, and sets the ACL to be private
resource "aws_s3_bucket" "shipment_picture_bucket" {
  bucket        = "shipment-picture-bucket"
  force_destroy = true
  lifecycle {
    prevent_destroy = false
  }
}


resource "aws_s3_bucket_acl" "shipment_picture_bucket_acl" {
  bucket = aws_s3_bucket.shipment_picture_bucket.id
  acl    = "private"
}

# dynamoDB table is created, with a primary key "shipmentId" and
# enables server-side encryption
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

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"
}

# populates table with sample data from file
resource "aws_dynamodb_table_item" "shipment" {
  for_each   = local.tf_data
  table_name = aws_dynamodb_table.shipment.name
  hash_key   = "shipmentId"
  item       = jsonencode(each.value)
}

# the bucket used for storing the lambda jar
resource "aws_s3_bucket" "lambda_code_bucket" {
  bucket        = "shipment-picture-lambda-validator-bucket"
  force_destroy = true
  lifecycle {
    prevent_destroy = false
  }
}

resource "aws_s3_bucket_acl" "lambda_code_bucket_acl" {
  bucket = aws_s3_bucket.lambda_code_bucket.id
  acl    = "private"
}

# bucket object with lambda code
resource "aws_s3_bucket_object" "lambda_code" {
  source = "../../shipment-picture-lambda-validator/target/shipment-picture-lambda-validator-1.0-SNAPSHOT.jar"
  bucket = aws_s3_bucket.lambda_code_bucket.id
  key    = "shipment-picture-lambda-validator-1.0-SNAPSHOT.jar"
}

# creates lambda using the JAR file uploaded to the S3 bucket.
# the function is set up with a java 11 runtime, with a specified IAM role,
# memory of 512mb, timeout of 15s, and environment variable
resource "aws_lambda_function" "shipment_picture_lambda_validator" {
  function_name = "shipment-picture-lambda-validator"
  handler       = "dev.ancaghenade.shipmentpicturelambdavalidator.ServiceHandler::handleRequest"
  runtime       = "java11"
  role          = aws_iam_role.lambda_exec.arn
  s3_bucket     = aws_s3_bucket.lambda_code_bucket.id
  s3_key        = aws_s3_bucket_object.lambda_code.key
  memory_size   = 512
  timeout       = 15
  environment {
    variables = {
      ENVIRONMENT = var.env
    }
  }
}


# notification for "shipment-picture-bucket" S3 bucket,
# so that the lambda function will be triggered when a new object is created in the bucket.
resource "aws_s3_bucket_notification" "demo_bucket_notification" {
  bucket = aws_s3_bucket.shipment_picture_bucket.id
  lambda_function {
    lambda_function_arn = aws_lambda_function.shipment_picture_lambda_validator.arn
    events              = ["s3:ObjectCreated:*"]
  }
}

resource "aws_lambda_permission" "s3_lambda_exec_permission" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.shipment_picture_lambda_validator.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.shipment_picture_bucket.arn
}

# IAM role with a policy that allows it to assume the role of a lambda function
# the role is attached to the Lambda function
resource "aws_iam_role" "lambda_exec" {
  name = "lambda_exec_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

# used to attach the AmazonS3FullAccess policy to the IAM role lambda_exec
resource "aws_iam_role_policy_attachment" "lambda_exec_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
  role       = aws_iam_role.lambda_exec.name
}

# used to create a custom IAM policy
# & give permission to the lambda to interract with the S3 and cloudwatch logs
resource "aws_iam_role_policy" "lambda_exec_policy" {
  name = "lambda_exec_policy"
  role = aws_iam_role.lambda_exec.id

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents"
            ],
            "Resource": "arn:aws:logs:*:*:*"
          },
          {
            "Effect": "Allow",
            "Action": [
              "s3:GetObject",
              "s3:PutObject"
            ],
            "Resource": [
              "arn:aws:s3:::shipment-picture-bucket",
              "arn:aws:s3:::shipment-picture-bucket/*"
            ]
          }
          ]
          }
          EOF
}


