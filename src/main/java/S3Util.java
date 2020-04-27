import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

/**
 * Very basic, slapped together util, to troubleshoot pushing a file to s3:
 *       using an acl (assumes a cross account bucket policy)
 *       or, assume role (assumes a cross account assume role setup)
 */
public class S3Util {
    private Regions clientRegion = Regions.AP_SOUTHEAST_2;

    private Context context;
//    private static final Logger LOG = LoggerFactory.getLogger(S3Utils.class);

    public S3Util(final Context context) {
        this.context = context;
    }

    public static void main(String[] args) {
        System.out.println("args.length = " + args.length);
        for (int i=0; i<args.length; i++) {
            System.out.printf("arg[%s] = [%s]\n",i, args[i]);
        }
        String localFileName = args[0];
        String bucketName = args[1];
        String assumeRoleName = args.length == 3 ? args[2] : null;

        Context context = new Context(localFileName, bucketName, assumeRoleName);
        S3Util s3util = new S3Util(context);
        s3util.listS3();
        if(args.length == 3){
            s3util.pushToS3WithAssumeRole();
        } else {
            s3util.pushToS3();
        }
        s3util.listS3();
    }

    /**
     * Push to S3, assuming using Bucket Policy.
     * uses ACL, BucketOwnerFullControl
     */
    public void pushToS3() {
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();
            System.out.printf("\nPushing [%s] to [%s]\n", context.localFileName, context.bucketName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(context.bucketName, context.fileKeyName, new File(context.localFileName))
                    .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);

            ObjectMetadata objectMetadata  = new ObjectMetadata();
            objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            putObjectRequest.setMetadata(objectMetadata);

            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("result contentMd5= " + result.getContentMd5());
        } catch (AmazonServiceException e) {
            // Request received by Amazon, but was unable to be processed. Error response returned
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted or client couldn't parse the response.
            e.printStackTrace();
        }
    }

    /**
     * Push file to S3, using an Assumed role in the target account.
     * nb: No need for ACL here, bucket owner of target account assumes permission
     */
    public void pushToS3WithAssumeRole() {
        try {
            String userName = System.getProperty("user.name");
            String hostName = InetAddress.getLocalHost().getHostName();
            String roleSessionName = userName + "@" + hostName;

            System.out.println("Using RoleSessionName = " + roleSessionName);

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider())
                    .withRegion(clientRegion)
                    .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(context.assumeRoleName)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
            System.out.printf("\nPushing (with assume role [%s]), file [%s] to [%s]\n", context.assumeRoleName, context.localFileName, context.bucketName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(context.bucketName, context.fileKeyName, new File(context.localFileName))
                    .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("result contentMd5= " + result.getContentMd5());

        } catch (AmazonServiceException e) {
            // Request received by Amazon, but was unable to be processed. Error response returned
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted or client couldn't parse the response.
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listS3() {
        System.out.format("Objects in S3 bucket %s:\n", context.bucketName);
        final AmazonS3 s3 = AmazonS3ClientBuilder
                                .standard()
                                .withRegion(Regions.AP_SOUTHEAST_2)
                                .build();
        ListObjectsV2Result result = s3.listObjectsV2(context.bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            System.out.println("* " + os.getKey());
        }
    }
}
