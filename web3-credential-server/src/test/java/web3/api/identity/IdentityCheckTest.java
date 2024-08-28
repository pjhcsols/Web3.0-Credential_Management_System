package web3.api.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IdentityCheckTest {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://development.codef.io/v1/kr/public/mw/identity-card/check-status";
    private static String accessToken;  // Access Token을 저장할 변수

    @BeforeAll
    static void setup() throws Exception {
        // AccessTokenTest 클래스를 이용하여 Access Token을 발급받음
        HashMap<String, String> tokenResponse = AccessTokenTest.publishToken(
                "9f515c3f-8df3-41b7-9da1-e08192131b3d", 
                "d0c5a8b8-2858-4059-acff-289f42892f47"
        );

        // 발급받은 토큰을 accessToken 변수에 저장
        if (tokenResponse != null && tokenResponse.get("access_token") != null) {
            accessToken = tokenResponse.get("access_token");
        } else {
            throw new RuntimeException("Failed to obtain access token");
        }
    }

    @Test
    void shouldCheckIdentityCardStatusSuccessfully() throws Exception {
        // API 요청 페이로드 준비
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("organization", "0002");
        requestBody.put("loginType", "0");
        requestBody.put("certType", "1");
        requestBody.put("certFile", "BASE64_ENCODED_CERT_DER"); // 실제 Base64 인코딩된 인증서 der 문자열로 대체
        requestBody.put("keyFile", "BASE64_ENCODED_KEY_FILE");  // 실제 Base64 인코딩된 키 파일 문자열로 대체
        requestBody.put("certPassword", "ENCRYPTED_PASSWORD");   // 실제 RSA 암호화된 비밀번호로 대체
        requestBody.put("loginTypeLevel", "1");
        requestBody.put("telecom", "");
        requestBody.put("phoneNo", "01012341234");
        requestBody.put("loginUserName", "홍길동");
        requestBody.put("loginIdentity", "9101011234123");
        requestBody.put("loginBirthDate", "");
        requestBody.put("birthDate", "");
        requestBody.put("identity", "9101011234123");
        requestBody.put("userName", "홍길동");
        requestBody.put("issueDate", "20190101");
        requestBody.put("identityEncYn", "");

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 발급받은 Access Token을 Authorization 헤더에 설정
        headers.setBearerAuth(accessToken);

        // HttpEntity 객체 생성 (요청 바디와 헤더 포함)
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // API 엔드포인트로 POST 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // URL 디코딩 수행 후 응답 바디 출력
            String decodedBody = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8.name());
            System.out.println("Decoded Response Body: " + decodedBody);

            // 응답 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(decodedBody).isNotNull();

            // URL 디코딩된 응답 파싱 (선택 사항: 특정 필드를 확인하기 위해)
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.readValue(decodedBody, Map.class);

            // 특정 응답 필드에 대한 검증 추가
            assertThat(responseBody).containsKey("result");
            assertThat(responseBody).containsKey("data");

        } catch (HttpStatusCodeException e) {
            // HTTP 상태 코드 에러를 처리하기 위해 예외 로그 출력
            System.err.println("HTTP Status Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
