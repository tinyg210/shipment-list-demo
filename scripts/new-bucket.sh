#!/bin/bash

# For demo purpose only;
# Access key and secret key are set by using `aws configure`
# to manage the aws cli on your local machine

# Set the name of the S3 bucket
bucket_name="shipment-list-demo-bucket"
aws_region="eu-central-1"

# Create the S3 bucket and set the region
aws s3api create-bucket --bucket $bucket_name --region eu-central-1 \
--create-bucket-configuration LocationConstraint=eu-central-1

# Set the bucket policy
aws s3api put-bucket-acl --bucket $bucket_name --acl private