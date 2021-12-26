## Introduction
Use this document to create DynamoDB table in your region, you could also refer to the lambda function to create the table.

## Create DynamoDB Table
```
aws dynamodb create-table \
    --table-name demo_user_profile \
    --key-schema '[
            {
                "KeyType": "HASH", 
                "AttributeName": "uid"
            }, 
            {
                "KeyType": "RANGE", 
                "AttributeName": "category_id"
            }
        ]' \
    --attribute-definitions '[
            {
                "AttributeName": "category_id", 
                "AttributeType": "S"
            }, 
            {
                "AttributeName": "uid", 
                "AttributeType": "S"
            }
        ]' \
    --billing-mode PROVISIONED \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

```