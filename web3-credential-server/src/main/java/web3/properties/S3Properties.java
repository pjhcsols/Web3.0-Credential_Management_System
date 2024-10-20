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
import software.amazon.awssdk.services.s3.model.S3Exception;

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
        try {
            this.s3Client = S3Client.builder()
                    .region(Region.of(regionStatic))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(credentialsAccessKey, credentialsSecretKey)))
                    .build();

            this.s3Client.listBuckets(); // 이 줄은 자격 증명과 지역을 검증합니다.

        } catch (S3Exception s3Ex) {
            log.error("S3 클라이언트 초기화 중 오류 발생: {} - {}", s3Ex.awsErrorDetails().errorCode(), s3Ex.awsErrorDetails().errorMessage());
            throw new IllegalStateException("S3 오류로 인해 S3 클라이언트를 초기화하지 못했습니다.", s3Ex);
        } catch (IllegalArgumentException iaEx) {
            log.error("S3 클라이언트 초기화 중 잘못된 인자 오류 발생: {}", iaEx.getMessage());
            throw new IllegalStateException("S3 클라이언트 초기화에 잘못된 인자가 제공되었습니다.", iaEx);
        } catch (Exception e) {
            log.error("S3 클라이언트 초기화 중 예기치 않은 오류 발생", e);
            throw new IllegalStateException("예기치 않은 오류로 인해 S3 클라이언트를 초기화하지 못했습니다.", e);
        }
    }

}
