package smarshare.coreservice.configuration;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreServiceConfiguration {


    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials( accessKey, secretKey );
            }

            @Override
            public void refresh() {

            }
        };
    }

    @Bean
    public AmazonS3 amazonS3Client(AWSCredentialsProvider awsCredentialsProvider, @Value("${cloud.aws.region.static}") String region) {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials( awsCredentialsProvider )
                .withRegion( region )
                .build();
    }

    @Bean
    public TransferManager transferManager(@Autowired AmazonS3 amazonS3Client) {
        return TransferManagerBuilder.standard()
                .withS3Client( amazonS3Client )
                .withMultipartUploadThreshold( (long) (5 * 1024 * 1025) )
                .build();
    }

}
