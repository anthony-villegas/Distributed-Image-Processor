#!/bin/bash

# Generate jar for Lambdas
cd ../image-processor-lambdas/ || exit
mvn clean package
cd ../cloudformation/ || exit

# Run AWS CloudFormation package
aws cloudformation package --template-file image-processor-template.yaml --s3-bucket ap-northeast-1-image-processor-lambda-bucket --s3-prefix lambdas --output-template-file infastructure-packaged.template

# Validate CloudFormation template
aws cloudformation validate-template --template-body file://infastructure-packaged.template

# Deploy CloudFormation stack
aws cloudformation deploy --template-file infastructure-packaged.template --stack-name ImageProcessorStack --region ap-northeast-1 --capabilities CAPABILITY_NAMED_IAM --parameter-overrides MetaDataDBUsername=db_user