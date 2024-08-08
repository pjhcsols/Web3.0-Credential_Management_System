package gift.controller.kakao;

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
