package web3.properties.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String tokenUrl,
        String clientId,
        String redirectUri,
        String userInfoUrl,
        String messageUrl,
        String codeUrl
) {
}
