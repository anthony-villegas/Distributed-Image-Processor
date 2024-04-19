#!/bin/bash

# Generate jar for Lambdas. This runs unit tests and local integration tests.
cd ../image-processor-lambdas/ || exit
mvn clean package
cd ../cloudformation/ || exit

# Run AWS CloudFormation package
aws cloudformation package --template-file image-processor-template.yaml --s3-bucket ap-northeast-1-image-processor-lambda-bucket --s3-prefix lambdas --output-template-file infastructure-packaged.template

# Validate CloudFormation template
aws cloudformation validate-template --template-body file://infastructure-packaged.template

# Deploy CloudFormation stack
aws cloudformation deploy --template-file infastructure-packaged.template --stack-name ImageProcessorStack --region ap-northeast-1 --capabilities CAPABILITY_NAMED_IAM --parameter-overrides MetaDataDBUsername=db_user

# Gather CloudFormation stack outputs
outputs=$(aws cloudformation describe-stacks --stack-name ImageProcessorStack --query 'Stacks[0].Outputs')

# Parse output values
secretName=$(aws cloudformation describe-stacks --stack-name ImageProcessorStack --query 'Stacks[0].Outputs[?OutputKey==`MetaDataDBSecretName`].OutputValue' --output text)
endpointAddress=$(aws cloudformation describe-stacks --stack-name ImageProcessorStack --query 'Stacks[0].Outputs[?OutputKey==`MetaDataDBEndpointAddress`].OutputValue' --output text)
dbName=$(aws cloudformation describe-stacks --stack-name ImageProcessorStack --query 'Stacks[0].Outputs[?OutputKey==`MetaDataDBName`].OutputValue' --output text)
region=$(aws cloudformation describe-stacks --stack-name ImageProcessorStack --query 'Stacks[0].Outputs[?OutputKey==`Region`].OutputValue' --output text)

# Pass output values as environment variables
export DB_SECRET_NAME=$secretName
export DB_ENDPOINT_ADDRESS=$endpointAddress
export DB_NAME=$dbName
export AWS_REGION=$region

# Run integration tests
cd ../image-processor-lambdas/ || exit
mvn failsafe:integration-test
cd ../cloudformation/ || exit