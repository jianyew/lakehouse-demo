#!/bin/bash

# set your bucket name and region
REGION=ap-southeast-1
BUCKET_NAME=aws-tdemo-deployment-test

aws s3api create-bucket \
    --bucket ${BUCKET_NAME} \
    --region ${REGION} \
    --create-bucket-configuration LocationConstraint=${REGION}
