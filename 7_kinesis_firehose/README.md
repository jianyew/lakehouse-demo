
```
aws iam create-role \
    --role-name firehose-delivery-role \
    --assume-role-policy-document file:///Users/wjianye/Documents/AWS/Projects/demo/demo_deployment/0_iam/Policies/firehose_assume_role_policy.json \
    --description "Allow Firehose to extract and save data to S3"


aws iam put-role-policy \
    --role-name firehose-delivery-role \
    --policy-name firehose-custom-policy \
    --policy-document file:///Users/wjianye/Documents/AWS/Projects/demo/demo_deployment/0_iam/Policies/firehose_custom_policy.json
```


```
aws firehose create-delivery-stream \
    --delivery-stream-name extract_message_parquet \
    --delivery-stream-type KinesisStreamAsSource \
    --kinesis-stream-source-configuration KinesisStreamARN=arn:aws:kinesis:ap-southeast-1:964479626419:stream/raw_events,RoleARN=arn:aws:iam::964479626419:role/firehose-delivery-role \
    --extended-s3-destination-configuration '{
  "RoleARN": "arn:aws:iam::964479626419:role/firehose-delivery-role",
  "BucketARN": "arn:aws:s3:::aws-demo-deployment-test",
  "Prefix": "processed_data/message/event_date=!{timestamp:yyyy-MM-dd}/",
  "ErrorOutputPrefix": "processed_data/firehose_errors/result=!{firehose:error-output-type}/event_date=!{timestamp:yyyy-MM-dd}/",
  "BufferingHints": {
    "SizeInMBs": 128,
    "IntervalInSeconds": 300
  },
  "CompressionFormat": "UNCOMPRESSED",
  "EncryptionConfiguration": {
    "NoEncryptionConfig": "NoEncryption"
  },
  "CloudWatchLoggingOptions": {
    "Enabled": true,
    "LogGroupName": "S3Delivery",
    "LogStreamName": "/aws/kinesisfirehose/extract_message_parquet"
  },
  "ProcessingConfiguration": {
    "Enabled": true,
    "Processors": [
    {    
        "Type": "Lambda",     
        "Parameters": [    
            {    
                "ParameterName": "LambdaArn",     
                "ParameterValue": "arn:aws:lambda:ap-southeast-1:964479626419:function:extract_message:$LATEST"    
            },     
            {    
                "ParameterName": "NumberOfRetries",     
                "ParameterValue": "3"
            },     
            {    
                "ParameterName": "RoleArn",     
                "ParameterValue": "arn:aws:iam::964479626419:role/firehose-delivery-role"
            },     
            {    
                "ParameterName": "BufferSizeInMBs",     
                "ParameterValue": "3"    
            },     
            {    
                "ParameterName": "BufferIntervalInSeconds",     
                "ParameterValue": "60"    
            }    
        ]    
    }    
    ]
  },
  "S3BackupMode": "Disabled",
  "DataFormatConversionConfiguration": {
    "SchemaConfiguration": {
      "RoleARN": "arn:aws:iam::964479626419:role/firehose-delivery-role",
      "DatabaseName": "demo",
      "TableName": "message",
      "Region": "ap-southeast-1",
      "VersionId": "LATEST"
    },
    "InputFormatConfiguration": {
      "Deserializer": {
        "OpenXJsonSerDe": {
          "ConvertDotsInJsonKeysToUnderscores": false,
          "CaseInsensitive": false
        }
      }
    },
    "OutputFormatConfiguration": {
      "Serializer": {
        "ParquetSerDe": {}
      }
    },
    "Enabled": true
  }
}'
```