## Introduction
Use this document to help you create Redshift cluster using aws-cli.

## Redshift
### Pre-requisites
- IAM role to load data from S3, later you can add more permission for the role to allow Redshift using Glue Catalog, i.e. enable Spectrum.
```
aws iam create-role \
    --role-name redshift-s3copy-role \
    --assume-role-policy-document file:///Users/wjianye/Documents/AWS/Projects/demo/demo_deployment/0_iam/Policies/redshift_assume_role_policy.json \
    --description "Allows Redshift to copy data from S3"

aws iam attach-role-policy \
    --role-name redshift-s3copy-role \
    --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess

```
- Security group for TCP 5439
```
aws ec2 create-security-group \
    --description "Redshift cluster port" \
    --group-name redshift-sg \
    --vpc-id vpc-a8e2efcf

# Put the security group id of last command in the following command
aws ec2 authorize-security-group-ingress \
    --group-id sg-0399a74111017c233 \
    --protocol tcp \
    --port 5439 \
    --cidr 54.240.196.174/32
```

### Create Redshift
This command would create a cluster in your default VPC, public accessable.
```
aws redshift create-cluster \
    --db-name dev \
    --cluster-identifier demo-search \
    --cluster-type single-node \
    --node-type dc2.large \
    --master-username demo \
    --master-user-password Awsome2020 \
    --vpc-security-group-ids sg-0399a74111017c233 \
    --publicly-accessible \
    --iam-roles arn:aws:iam::964479626419:role/redshift-s3copy-role

```

### Redshift Table setup
You login on the cluster either using client or the web console, then execute the command from `redshift.ddl`