
使用 `emr_create_cluster.sh` 创建 EMR cluster.

aws ec2 create-security-group \
    --description "Allow ssh access" \
    --group-name ssh-only \
    --vpc-id vpc-a8e2efcf

aws ec2 authorize-security-group-ingress \
    --group-id sg-0f677fbf058565cc3 \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0


aws ec2 describe-instance-attribute \
    --instance-id i-063d1e7eb4db28ab6 \
    --attribute groupSet

## 配置sg到master 
aws ec2 modify-instance-attribute \
    --instance-id i-063d1e7eb4db28ab6 \
    --groups sg-0f677fbf058565cc3 sg-05500ed08d5673951

# execute the code as EMR step
aws s3 cp hive.ddl s3://aws-tdemo-deployment-test/codes/

aws emr add-steps --cluster-id j-UN5ZANP1GBBN \
--steps Type=HIVE,Name='Hive DDL',ActionOnFailure=CONTINUE,Args=[-f,s3://aws-tdemo-deployment-test/codes/hive.ddl]

# For future usage
aws s3 cp hive.sql s3://aws-tdemo-deployment-test/codes/

