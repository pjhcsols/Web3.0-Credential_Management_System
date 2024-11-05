package web3.s3Storage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import web3.properties.S3Properties;
/*
@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final S3Properties s3Properties;
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegionStatic()))
                .build();
    }

}

 */