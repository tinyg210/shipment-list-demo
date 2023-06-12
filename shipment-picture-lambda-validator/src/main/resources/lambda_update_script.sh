
#you need awslocal cli installed for this

awslocal lambda update-function-code --function-name shipment-picture-lambda-validator \
         --zip-file fileb://target/shipment-picture-lambda-validator.jar \
         --region us-east-1

aws lambda update-function-code --function-name shipment-picture-lambda-validator \
         --zip-file fileb://target/shipment-picture-lambda-validator.jar \
         --region us-east-1