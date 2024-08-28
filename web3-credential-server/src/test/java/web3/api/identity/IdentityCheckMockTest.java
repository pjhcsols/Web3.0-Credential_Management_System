package web3.api.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.InfoProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
public class IdentityCheckMockTest {

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private InfoProperties identityProperties;

    @Autowired
    private MockRestServiceServer mockServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockServer.reset();
    }

    @Test
    void testIdentityCardCheckStatusSuccess() throws Exception {
        String apiUrl = "https://development.codef.io/v1/kr/public/mw/identity-card/check-status";
        String certFileBase64 = "BASE64로 Encoding된 인증서 der파일 문자열";
        String keyFileBase64 = "BASE64로 Encoding된 인증서 key파일 문자열";
        String certPassword = "RSA암호화된 비밀번호";
        String identity = "9101011234123";
        String userName = "홍길동";
        String issueDate = "20190101";

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0002\",\n" +
                "    \"loginType\": \"0\",\n" +
                "    \"certType\": \"1\",\n" +
                "    \"certFile\": \"" + certFileBase64 + "\",\n" +
                "    \"keyFile\": \"" + keyFileBase64 + "\",\n" +
                "    \"certPassword\": \"" + certPassword + "\",\n" +
                "    \"identity\": \"" + identity + "\",\n" +
                "    \"userName\": \"" + userName + "\",\n" +
                "    \"issueDate\": \"" + issueDate + "\",\n" +
                "    \"identityEncYn\": \"N\"\n" +
                "}";

        // 모의 응답 설정
        String successResponse = "{\n" +
                "    \"resUserNm\": \"" + userName + "\",\n" +
                "    \"resUserIdentiyNo\": \"910101-1234***\",\n" +
                "    \"resAuthenticity\": \"1\",\n" +
                "    \"resAuthenticityDesc\": \"주민등록번호 진위확인 성공\"\n" +
                "}";

        mockServer.expect(requestTo(apiUrl))
                .andExpect(content().json(requestBody))
                .andRespond(withSuccess(successResponse, MediaType.APPLICATION_JSON));

        // 실제 요청 수행
        String response = requestIdentityCheck(apiUrl, requestBody);

        // 응답 검증
        JsonNode jsonResponse = objectMapper.readTree(response);
        assertThat(jsonResponse.get("resUserNm").asText()).isEqualTo(userName);
        assertThat(jsonResponse.get("resAuthenticity").asText()).isEqualTo("1");
        assertThat(jsonResponse.get("resAuthenticityDesc").asText()).isEqualTo("주민등록번호 진위확인 성공");
    }

    @Test
    void testIdentityCardCheckStatusFailure() {
        String apiUrl = "https://development.codef.io/v1/kr/public/mw/identity-card/check-status";
        String certFileBase64 = "INVALID_CERT_FILE";
        String keyFileBase64 = "INVALID_KEY_FILE";
        String certPassword = "INVALID_PASSWORD";
        String identity = "9101011234123";
        String userName = "홍길동";
        String issueDate = "20190101";

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0002\",\n" +
                "    \"loginType\": \"0\",\n" +
                "    \"certType\": \"1\",\n" +
                "    \"certFile\": \"" + certFileBase64 + "\",\n" +
                "    \"keyFile\": \"" + keyFileBase64 + "\",\n" +
                "    \"certPassword\": \"" + certPassword + "\",\n" +
                "    \"identity\": \"" + identity + "\",\n" +
                "    \"userName\": \"" + userName + "\",\n" +
                "    \"issueDate\": \"" + issueDate + "\",\n" +
                "    \"identityEncYn\": \"N\"\n" +
                "}";

        // 모의 응답 설정: 400 BAD REQUEST
        mockServer.expect(requestTo(apiUrl))
                .andExpect(content().json(requestBody))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        try {
            requestIdentityCheck(apiUrl, requestBody);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to request identity card check status");
        }
    }

    private String requestIdentityCheck(String url, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request identity card check status", e);
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public MockRestServiceServer mockRestServiceServer(RestTemplate restTemplate) {
            return MockRestServiceServer.createServer(restTemplate);
        }
    }
}