
#you need awslocal cli installed for this

awslocal lambda update-function-code --function-name shipment-picture-lambda-validator \
         --zip-file fileb://target/shipment-picture-lambda-validator-1.0-SNAPSHOT.jar \
         --region eu-central-1