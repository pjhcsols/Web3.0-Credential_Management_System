package web3.api.passport;

/*
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import web3.api.passport.config.RestTemplateConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {PassportValidityApplication.class, RestTemplateConfig.class}) 
public class PassportValidityTest {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ACCESS_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String API_URL = "https://development.codef.io/v1/kr/public/mw/passport-data/status";

    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

    @Test
    void shouldCheckPassportValiditySuccessfully() throws Exception {
        
        // Get Access Token
        HashMap<String, String> tokenResponse = publishToken(CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenResponse).isNotNull();
        String accessToken = tokenResponse.get("access_token");
        assertThat(accessToken).isNotNull();

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0002\",\n" +
                "    \"loginType\": \"0\",\n" +
                "    \"certFile\": \"BASE64_인코딩된_인증서_문자열\",\n" +
                "    \"keyFile\": \"BASE64_인코딩된_키_문자열\",\n" +
                "    \"certPassword\": \"RSA_암호화된_비밀번호\",\n" +
                "    \"certType\": \"1\",\n" +
                "    \"userName\": \"홍길동\",\n" +
                "    \"passportNo\": \"\",\n" +
                "    \"issueDate\": \"20190101\",\n" +
                "    \"expirationDate\": \"20240101\",\n" +
                "    \"birthDate\": \"19900707\"\n" +
                "}";

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // HttpEntity 객체 생성 (요청 바디와 헤더 포함)
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // API 엔드포인트로 POST 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 문자열 응답 본문 출력
            String responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);

            // Content-Type 확인
            MediaType contentType = response.getHeaders().getContentType();
            System.out.println("Content-Type: " + contentType);

            // 응답 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseBody).isNotNull();

            // JSON 파싱 시도 (Content-Type이 JSON일 경우만)
            if (contentType != null && contentType.equals(MediaType.APPLICATION_JSON)) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> parsedResponseBody = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

                // 필드 검증
                assertThat(parsedResponseBody).containsKey("resAuthenticity");
                assertThat(parsedResponseBody.get("resAuthenticity")).isInstanceOf(String.class);
                String resAuthenticity = (String) parsedResponseBody.get("resAuthenticity");
                assertThat(resAuthenticity).isNotEmpty();
            } else {
                System.err.println("Unexpected content type: " + contentType);
            }

        } catch (Exception e) {
            // 오류 로그 출력
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    protected static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        try {
            URL url = new URL(ACCESS_TOKEN_URL);
            String params = "grant_type=client_credentials&scope=read";

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = clientId + ":" + clientSecret;
            String authStringEnc = Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + authStringEnc;

            con.setRequestProperty("Authorization", authHeader);
            con.setDoInput(true);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder responseStr = new StringBuilder();
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        responseStr.append(inputLine);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(responseStr.toString(), new TypeReference<HashMap<String, String>>() {});
                }
            } else {
                System.out.println("Failed to get access token: HTTP error code : " + responseCode);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


 */