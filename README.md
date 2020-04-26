# AWS S3 util test class

Simple java project, to test uploading a file to S3 with...

* Setting an ACL (Assuming a cross account Bucket Policy Setup)
* Assuming a role (Assumes a cross account role setup)

... after, in my case, configuring a cross account bucket policy and/or a cross account role.

---

## Pre-requisites
* java 1.8
* maven
* an existing s3 bucket
* an existing cross account bucket (or role)

---

## Running
Execute the following:

    AWS_PROFILE=sandbox \
        make run \
            BUCKET_NAME="my-bucket-name" \
            ROLE_TO_ASSUME_NAME="my-role-to-assume"

This will:
* Build the project with mvn
* Run the java app, to put the file in the target bucket
* If `ROLE_TO_ASSUME` is NOT provided, the app will simply push directly to the bucket (assumes a cross account bucket policy has been setup), and use an ACL during the PUT.
* If `ROLE_TO_ASSUME` IS provided, credentials for that role will be retrived and used for the subsequent push.
   