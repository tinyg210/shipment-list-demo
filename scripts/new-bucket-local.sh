#!/bin/bash

# Set the name of the S3 bucket
bucket_name="shipment-list-demo-bucket"
aws_region="eu-central-1"

# Create the S3 bucket
awslocal s3api create-bucket --bucket $bucket_name
