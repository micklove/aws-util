import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;
import java.util.List;


public class S3Util {

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
            s3util.pushToS3();
        } else {
            s3util.pushToS3WithAssumeRole();
        }
        s3util.listS3();
    }

    public void pushToS3() {
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .build();
            System.out.printf("\nPushing [%s] to [%s]\n", context.localFileName, context.bucketName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(context.bucketName, context.fileKeyName, new File(context.localFileName))
                    .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            result.toString();

        } catch (AmazonServiceException e) {
            // Request received by Amazon, but was unable to be processed. Error response returned
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted or client couldn't parse the response.
            e.printStackTrace();
        }
    }

    public void pushToS3WithAssumeRole() {
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .build();
            System.out.printf("\nPushing [%s] to [%s]\n", context.localFileName, context.bucketName);
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
