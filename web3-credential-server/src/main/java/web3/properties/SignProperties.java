package web3.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Getter
@Configuration
@ConfigurationProperties(prefix = "sign")
public class SignProperties {

    private static final Logger logger = LoggerFactory.getLogger(SignProperties.class);

    private String signCertDir;
    private String signPriDir;
    private String fullSignCertDir;
    private String fullSignPriDir;

    @PostConstruct
    private void init() {
        try {
            // 파일 시스템 경로로 절대 경로 변환
            this.fullSignCertDir = ensureTrailingSlash(new File(signCertDir).getAbsolutePath());
            this.fullSignPriDir = ensureTrailingSlash(new File(signPriDir).getAbsolutePath());

            logger.info("Full Upload Dir: {}", fullSignCertDir);
            logger.info("Full Profile Dir: {}", fullSignPriDir);
        } catch (Exception e) {
            logger.error("리소스를 찾을 수 없습니다: {}", e.getMessage());
            throw new RuntimeException("리소스를 찾을 수 없습니다", e);
        }
    }

    private String ensureTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public void setSignCertDir(String signCertDir) {
        this.signCertDir = ensureTrailingSlash(signCertDir);
    }

    public void setSignPriDir(String signPriDir) {
        this.signPriDir = ensureTrailingSlash(signPriDir);
    }

}
