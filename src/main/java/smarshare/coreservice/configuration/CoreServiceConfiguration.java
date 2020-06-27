package smarshare.coreservice.configuration;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CoreServiceConfiguration {


    private final String accessKey;
    private final String secretKey;
    private final String region;

    public CoreServiceConfiguration(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                                    @Value("${cloud.aws.credentials.secret-key}") String secretKey,
                                    @Value("${cloud.aws.region.static}") String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }


    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( this.accessKey, this.secretKey ) ) )
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
