package web3.api.passport;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class PassportValidityTest {

    private WebClient webClient = WebClient.builder()
            .baseUrl("https://development.codef.io")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private static final String ACCESS_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String API_URL = "/v1/kr/public/mw/passport-data/status";

    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";


    //codef api 계정의 public Key 입력하기
    private String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGXundpJlAHNhwDiVDSKWy4iJ+jzkawCMy3d1IZ0o5qHaOk8X2bVn9rL8lepioNNGcswWxhjs4UEqPGsu6+XPHbbYrUPNimlaa0dRsQcNdjD7flaSMIbDMeD5v04AZiquWcLZl1CqdzntLeYXVat7uqcQ68Sb5mGn0HYWN8XlQHpBMdmEESr0mJCEhLI2MD6+uqU8oMnrUnPJZSkKD83udCXjt1b0N8SksWBtWz3NQqsmx8a9NgYJlRSG1jkI8zgBzwtvnNxD4NaM/NqtDiuVXhNfupltzmA+xt4hy+DD00GKUcg05iRQih1go3WG8UKtA5KOqcfHS9e8S9z77lkkQIDAQAB"; //public key 넣기
  

    @Test
    void shouldCheckPassportValiditySuccessfully() throws Exception {

        // Get Access Token
        HashMap<String, String> tokenResponse = publishToken(CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenResponse).isNotNull();
        String accessToken = tokenResponse.get("access_token");
        assertThat(accessToken).isNotNull();

/////////////////////////////////////////////////여기부터///////////////////////////////


        String certFileEncoded = "";
        String keyFileEncoded = "";

        // 사용자의 공동 인증서 비밀번호
        String certPassword = "";


        //사용자 실제값
        String userName = "";
        String identity= "";
        String passportNo= "";
        String issueDate = "";
        String expirationDate ="";
        String brithDate= "";

//////////////////////////////////////////////여기까지는 개인정보 유출 주의!!!/////////////////////

        String rsaEncryptedPassword = encryptRSAPassword(certPassword);

        // JSON Request Payload
        String requestBody = "{\n" +
                    "    \"organization\": \"0002\",\n" +
                    "    \"loginType\": \"2\",\n" +
                    "    \"certType\": \"1\",\n" +
                    "    \"certFile\": \"" + certFileEncoded + "\",\n" +
                    "    \"keyFile\": \"" + keyFileEncoded + "\",\n" +
                    "    \"certPassword\": \"" + rsaEncryptedPassword + "\",\n" +
                    "    \"userName1\": \"" + userName + "\",\n" +
                    "    \"identity\": \"" + identity + "\",\n" +
                    "    \"userName\": \"" + userName + "\",\n" +
                    "    \"passportNo\": \"" + passportNo + "\",\n" +
                    "    \"issueDate\": \"" + issueDate + "\",\n" +
                    "    \"expirationDate\": \"" + expirationDate + "\",\n" +
                    "    \"birthDate\": \"" + brithDate + "\"\n" +
                "}";

        // WebClient로 POST 요청 보내기
        String response = webClient.post()
                .uri(API_URL)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();  // 비동기 호출을 동기화하여 응답을 즉시 받음

        // URL 인코딩된 응답 본문 디코딩
        String responseBody = URLDecoder.decode(response, StandardCharsets.UTF_8);
        System.out.println("Decoded Response Body: " + responseBody);

        // Content-Type 확인
        MediaType contentType = MediaType.parseMediaType("application/json"); // 응답 헤더에서 얻는 대신 지정
        System.out.println("Content-Type: " + contentType);

        assertThat(responseBody).isNotNull();

    }

    
    public String encryptRSAPassword(String password) throws Exception {
        // 1. Base64로 인코딩된 공개키 문자열을 PublicKey 객체로 변환
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 2. Cipher 객체를 사용하여 RSA 암호화 수행
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // PKCS1Padding 사용
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // 3. 비밀번호를 암호화
        byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));

        // 4. 암호화된 바이트 배열을 Base64로 인코딩하여 반환
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    protected static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        WebClient tokenClient = WebClient.builder()
                .baseUrl(ACCESS_TOKEN_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()))
                .build();

        return tokenClient.post()
                .bodyValue("grant_type=client_credentials&scope=read")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<HashMap<String, String>>() {
                })
                .block();  // 동기화하여 토큰을 즉시 얻음
    }
}
