#!/bin/bash

# For demo purpose only;
# Access key and secret key are set by using `aws configure`
# to manage the aws cli on your local machine


# Create the S3 bucket and set the region
aws s3api create-bucket --bucket shipment-list-demo-bucket --region eu-central-1 \
--create-bucket-configuration LocationConstraint=eu-central-1

# Set the bucket policy
aws s3api put-bucket-acl --bucket shipment-list-demo-bucket --acl private