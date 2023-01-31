#!/bin/bash

# Create the S3 bucket
awslocal s3api create-bucket --bucket shipment-list-demo-bucket
