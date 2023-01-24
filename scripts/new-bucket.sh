#!/bin/bash

# For demo purpose only;
# Access key and secret key are set and using `aws configure`
# to manage the aws cli on your local machine

# Set the name of the S3 bucket
bucket_name="shipment-list-demo-bucket"
aws_region="eu-central-1"

# Create the S3 bucket
aws s3 mb s3://$bucket_name

# Set the region for the S3 bucket
aws s3api create-bucket --bucket $bucket_name --region $aws_region

# Set the bucket policy
aws s3api put-bucket-acl --bucket $bucket_name --acl private
