


创建glue role
```
aws iam create-role \
    --role-name glue-execution-role \
    --assume-role-policy-document file:///Users/wjianye/Documents/AWS/Projects/demo/demo_deployment/0_iam/Policies/glue_assume_role_policy.json \
    --description "Allows Glue to call AWS services on your behalf"

aws iam attach-role-policy \
    --role-name glue-execution-role \
    --policy-arn arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole

```

创建glue job
```
aws glue create-job --name glue-extract-message \
    --role "arn:aws:iam::964479626419:role/glue-execution-role" \
    --command Name=glueetl,ScriptLocation=s3://aws-demo-deployment-test/codes/aws-glue-jobs/glue_extract_message.py,PythonVersion=3 \
    --description "Batch extract message from S3" \
    --max-capacity 5 \
    --glue-version 1.0 \
    --default-arguments="--TempDir=s3://aws-demo-deployment-test/aws-glue-logs,--spark-event-logs-path=s3://aws-demo-deployment-test/aws-glue-spark-logs/,--enable-spark-ui=true,--enable-continuous-cloudwatch-log=true"

```
