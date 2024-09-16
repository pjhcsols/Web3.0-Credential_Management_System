package web3.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConfigurationProperties(prefix = "cloud.aws")
@Getter
@Setter
@Slf4j
public class S3Properties {

    private String s3BucketName;
    private String credentialsAccessKey;
    private String credentialsSecretKey;
    private String s3BucketUrl;
    private String regionStatic;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (regionStatic == null || regionStatic.trim().isEmpty()) {
            throw new IllegalStateException("Region must not be null or empty");
        }
        this.s3Client = S3Client.builder()
                .region(Region.of(regionStatic))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(credentialsAccessKey, credentialsSecretKey)))
                .build();
    }

}
