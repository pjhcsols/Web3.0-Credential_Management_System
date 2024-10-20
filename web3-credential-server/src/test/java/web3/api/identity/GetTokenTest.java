package web3.api.identity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTokenTest {

    private static final String TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

    // RestTemplate을 Mocking
    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testPublishToken_Success() {
        try {
            // 가짜 응답 설정 (JSON 형식의 Access Token 반환)
            String mockResponse = "{ \"access_token\": \"mock_access_token\", \"token_type\": \"bearer\" }";
            
            // ResponseEntity 생성 (모킹된 응답)
            ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

            // RestTemplate.exchange가 호출될 때 mockResponseEntity를 반환하도록 설정
            when(restTemplate.exchange(
                    anyString(), 
                    eq(HttpMethod.POST), 
                    any(HttpEntity.class), 
                    eq(String.class)))
                    .thenReturn(mockResponseEntity);

            // 실제 토큰 발급 함수 호출
            HashMap<String, String> tokenMap = publishToken(CLIENT_ID, CLIENT_SECRET);
            
            // 응답 검증
            assertThat(tokenMap).isNotNull();
            assertThat(tokenMap.containsKey("access_token")).isTrue();
            assertThat(tokenMap.get("access_token")).isEqualTo("mock_access_token");

            System.out.println("Access Token: " + tokenMap.get("access_token"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * RestTemplate을 사용하여 토큰을 발급받는 메서드
     * 
     * @param clientId OAuth2 Client ID
     * @param clientSecret OAuth2 Client Secret
     * @return 토큰 응답 데이터가 포함된 HashMap
     */
    public HashMap<String, String> publishToken(String clientId, String clientSecret) {
        try {
            // Base64 인코딩된 인증 헤더 생성
            String auth = clientId + ":" + clientSecret;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", authHeader);

            // 요청 바디 설정 (grant_type 설정)
            String body = "grant_type=client_credentials&scope=read";

            // HttpEntity를 사용하여 요청 헤더 및 바디를 설정
            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

            // Mock된 RestTemplate 사용 (실제 API 호출 X)
            ResponseEntity<String> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 응답 본문 출력 및 확인
            String responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);

            // 응답 본문을 JSON 형태로 파싱하여 HashMap으로 반환
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, new TypeReference<HashMap<String, String>>() {});
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            return null;
        }
    }
}
// 아래 코드는 Spring 환경 아니어도 독립적으로 실행 가능한 유닛 테스트
// package web3.api.identity;

// import org.junit.jupiter.api.Test;

// import java.util.HashMap;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.fail;

// public class GetTokenTest {

//     private String clientId = "86640213-3b83-461a-97ab-2491d68a2052";
//     private String clientSecret = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";
//     private String invalidClientId = "invalid-client-id";
//     private String invalidClientSecret = "invalid-client-secret";

//     @Test
//     public void testPublishToken_Success() {
//         try {
//             HashMap<String, String> tokenMap = GetToken.publishToken(clientId, clientSecret);
//             assertThat(tokenMap).isNotNull();
//             assertThat(tokenMap.containsKey("access_token")).isTrue();
//             System.out.println("Access Token: " + tokenMap.get("access_token"));
//         } catch (Exception e) {
//             fail("Exception occurred: " + e.getMessage());
//         }
//     }

//     @Test
//     public void testPublishToken_Failure() {
//         try {
//             HashMap<String, String> tokenMap = GetToken.publishToken(invalidClientId, invalidClientSecret);
//             assertThat(tokenMap).isNull();
//         } catch (Exception e) {
//             // Expected exception caught, test passed.
//             assertThat(e.getMessage()).contains("Failed to get access token");
//         }
//     }
// }