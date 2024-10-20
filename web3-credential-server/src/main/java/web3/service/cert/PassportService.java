package web3.service.cert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import web3.properties.PassportProperties;
import web3.service.dto.cert.PassportRequestDto;

import javax.crypto.Cipher;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PassportService {

    private final PassportProperties passportProperties;
    private final WebClient webClient;
    private final WebClient webClientToken; // 액세스 토큰 요청을 위한 WebClient
    private static final String BASE_URL = "https://development.codef.io";
    private static final String API_URL = "/v1/kr/public/mw/passport-data/status";
    private static final String BASE_TOKEN_URL = "https://oauth.codef.io";
    private static final String ACCESS_TOKEN_URL = "/oauth/token";
    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";

    public PassportService(WebClient.Builder webClientBuilder, PassportProperties passportProperties) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.webClientToken = webClientBuilder.baseUrl(BASE_TOKEN_URL).build();
        this.passportProperties = passportProperties;
    }

    // 액세스 토큰을 가져오는 메서드
    private Mono<String> fetchAccessToken() {
        return webClientToken.post()
                .uri(ACCESS_TOKEN_URL)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + passportProperties.clientSecret()).getBytes()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&scope=read")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .doOnError(e -> log.error("Error fetching access token: {}", e.getMessage()));
    }

    // RSA 암호화 메서드
    private String encryptRSAPassword(String password) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(passportProperties.publicKeyStr());
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
    }

    public Mono<Map<String, Object>> checkPassportValidity(PassportRequestDto passportRequestDto) {
        return fetchAccessToken()
                .flatMap(accessToken -> {
                    try {
                        Map<String, String> requestBody = createRequestBody(passportRequestDto);
                        return webClient.post()
                                .uri(API_URL)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMap(this::parseResponse);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    private Mono<Map<String, Object>> parseResponse(String responseBody) {
        try {
            String decodedResponseBody = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return Mono.just(objectMapper.readValue(decodedResponseBody, new TypeReference<Map<String, Object>>() {}));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private Map<String, String> createRequestBody(PassportRequestDto passportRequestDto) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("organization", "0002");
        requestBody.put("loginType", "2");
        requestBody.put("certType", "1");
        requestBody.put("certFile", passportRequestDto.getCertFileEncoded());
        requestBody.put("keyFile", passportRequestDto.getKeyFileEncoded());
        requestBody.put("certPassword", encryptRSAPassword(passportRequestDto.getCertPassword()));
        requestBody.put("userName", passportRequestDto.getUserName());
        requestBody.put("userName1", passportRequestDto.getUserName());
        requestBody.put("identity", passportRequestDto.getIdentity());
        requestBody.put("passportNo", passportRequestDto.getPassportNo());
        requestBody.put("issueDate", passportRequestDto.getIssueDate());
        requestBody.put("expirationDate", passportRequestDto.getExpirationDate());
        requestBody.put("birthDate", passportRequestDto.getBirthDate());
        return requestBody;
    }
}
