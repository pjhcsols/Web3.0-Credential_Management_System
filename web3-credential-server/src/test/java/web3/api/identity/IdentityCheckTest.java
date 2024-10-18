package web3.api.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdentityCheckTest {

    @MockBean
    private RestTemplate restTemplate;

    private static final String API_URL = "https://development.codef.io/v1/kr/public/mw/identity-card/check-status";
    private static String accessToken;

    private MockRestServiceServer mockServer;

    @BeforeAll
    static void setup() {
        accessToken = "mock_access_token"; // Mock access token
    }

    @Autowired
    public void setUpMockServer(RestTemplate restTemplate) {
        // Initialize MockRestServiceServer
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldCheckIdentityCardStatusSuccessfully() throws Exception {
        // Mock API response setup
        String mockApiResponse = "{ \"result\": \"success\", \"data\": { \"resUserNm\": \"홍길동\", \"resUserIdentiyNo\": \"910101-1234***\", \"resAuthenticity\": \"1\", \"resAuthenticityDesc\": \"주민등록번호 진위확인 성공\" } }";
        mockServer.expect(requestTo(API_URL))
                .andExpect(method(POST)) // Correctly specify the HTTP method
                .andRespond(withSuccess(mockApiResponse, MediaType.APPLICATION_JSON));

        // API request payload preparation
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("organization", "0002");
        requestBody.put("loginType", "0");
        requestBody.put("certType", "1");
        requestBody.put("certFile", "BASE64_ENCODED_CERT_DER"); // Replace with actual Base64 encoded cert
        requestBody.put("keyFile", "BASE64_ENCODED_KEY_FILE");  // Replace with actual Base64 encoded key
        requestBody.put("certPassword", "ENCRYPTED_PASSWORD");   // Replace with actual encrypted password
        requestBody.put("loginUserName", "홍길동");
        requestBody.put("loginIdentity", "9101011234123");
        requestBody.put("issueDate", "20190101");
        requestBody.put("identityEncYn", "N");

        // HTTP request header setup
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // HttpEntity object creation (including request body and headers)
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make API call and receive response
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                POST,
                requestEntity,
                String.class
        );

        // Assert the response is not null and has the expected status code
        assertThat(response).isNotNull();  // Ensure response is not null
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check the mock server for expected behavior
        mockServer.verify();  // Verify that the request was received by the mock server

        // Ensure the response body is not null and decode it
        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull(); // Ensure response body is not null

        // URL decode the response body
        String decodedBody = URLDecoder.decode(responseBody, StandardCharsets.UTF_8.name());
        System.out.println("Decoded Response Body: " + decodedBody);

        // URL decoded response parsing
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(decodedBody, Map.class);

        // Assertions on response fields
        assertThat(responseMap).containsKey("result");
        assertThat(responseMap).containsKey("data");
        assertThat(((Map<String, Object>) responseMap.get("data")).get("resUserNm")).isEqualTo("홍길동");
        assertThat(((Map<String, Object>) responseMap.get("data")).get("resAuthenticityDesc")).isEqualTo("주민등록번호 진위확인 성공");
    }
}
