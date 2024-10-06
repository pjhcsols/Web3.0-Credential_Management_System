package web3.api.identity;

import com.fasterxml.jackson.core.util.RequestPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdentityCheckMockTest {

    @MockBean
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockServer = MockRestServiceServer.createServer(restTemplate);
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

        // Create JSON Request Payload using ObjectMapper
        String requestBody = objectMapper.writeValueAsString(new RequestPayload("0002"));

        // Mock success response
        String successResponse = "{\n" +
                "    \"resUserNm\": \"" + userName + "\",\n" +
                "    \"resUserIdentiyNo\": \"910101-1234***\",\n" +
                "    \"resAuthenticity\": \"1\",\n" +
                "    \"resAuthenticityDesc\": \"주민등록번호 진위확인 성공\"\n" +
                "}";

        mockServer.expect(requestTo(apiUrl))
                .andExpect(content().json(requestBody))
                .andRespond(withSuccess(successResponse, MediaType.APPLICATION_JSON));

        // Actual request execution
        String accessToken = "YOUR_ACCESS_TOKEN"; // Insert actual access token here
        String response = requestIdentityCheck(apiUrl, requestBody, accessToken);

        // Response verification
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
        String certPassword = "INALID_PASSWORD";
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

        // Mock response: 400 BAD REQUEST
        mockServer.expect(requestTo(apiUrl))
                .andExpect(content().json(requestBody))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        try {
            String accessToken = "YOUR_ACCESS_TOKEN"; // Insert actual access token here
            requestIdentityCheck(apiUrl, requestBody, accessToken);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to request identity card check status");
        }
    }

    private String requestIdentityCheck(String url, String body, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);  // Set the Bearer token in the headers
            return restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request identity card check status", e);
        }
    }
}