package gift.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.controller.kakao.KakaoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
public class RestClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private KakaoProperties kakaoProperties;

    @Autowired
    private MockRestServiceServer mockServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockServer.reset();
    }

    @Test
    void testKakaoLoginFlowSuccess() throws Exception {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        String clientId = kakaoProperties.clientId();
        String redirectUri = kakaoProperties.redirectUri();
        String authorizationCode = "YZ6yhu37sPGFAm0pg5-Ip7Wua2WdGF1W37rAYBvmnRQU-1yaQ0ngvwAAAAQKPXRpAAABkOJv3OjHP8VuE1ZNOQ";

        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);

        // 모의 응답 설정
        String tokenResponse = "{\"access_token\": \"test-access-token\"}";
        mockServer.expect(requestTo(tokenUrl))
                .andRespond(withSuccess(tokenResponse, MediaType.APPLICATION_JSON));

        String userInfoResponse = "{\"properties\": {\"nickname\": \"test-nickname\"}, \"kakao_account\": {\"email\": \"test-email@example.com\"}}";
        mockServer.expect(requestTo(userInfoUrl + "?access_token=test-access-token"))
                .andRespond(withSuccess(userInfoResponse, MediaType.APPLICATION_JSON));

        // 실제 요청 수행
        var response = requestAccessToken(tokenUrl, body);
        String accessToken = extractAccessToken(response);
        var userInfo = getUserInfo(accessToken);

        // 검증
        assertThat(response).contains("access_token");
        assertThat(accessToken).isEqualTo("test-access-token");
        assertThat(userInfo.get("nickname")).isEqualTo("test-nickname");
        assertThat(userInfo.get("email")).isEqualTo("test-email@example.com");
    }


    @Test
    void testKakaoLoginFlowFailure() {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        String clientId = kakaoProperties.clientId();
        String redirectUri = kakaoProperties.redirectUri();
        String authorizationCode = "invalid-authorization-code";

        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);

        // 모의 응답 설정: 400 BAD REQUEST
        mockServer.expect(requestTo(tokenUrl))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        try {
            requestAccessToken(tokenUrl, body);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to request access token");
        }
    }



    private String requestAccessToken(String url, LinkedMultiValueMap<String, String> body) {
        try {
            return restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request access token", e);
        }
    }

    private String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract access token", e);
        }
    }

    private HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            String response = restTemplate.getForObject(reqURL + "?access_token=" + accessToken, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);

            JsonNode properties = jsonNode.path("properties");
            JsonNode kakaoAccount = jsonNode.path("kakao_account");

            userInfo.put("nickname", properties.path("nickname").asText("Unknown"));
            userInfo.put("email", kakaoAccount.path("email").asText("Unknown"));

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user info", e);
        }

        return userInfo;
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

