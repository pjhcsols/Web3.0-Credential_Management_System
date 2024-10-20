package web3.api.qnet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootApplication
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QnetCertificateStatusTest {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ACCESS_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String API_URL = "https://development.codef.io/v1/kr/etc/hr/qnet-certificate/status";  // 수정된 도메인

    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

    @Test
    void shouldCheckQnetCertificateStatusSuccessfully() throws Exception {

        // Get Access Token
        HashMap<String, String> tokenResponse = publishToken(CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenResponse).isNotNull();
        String accessToken = tokenResponse.get("access_token");
        assertThat(accessToken).isNotNull();

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0001\",\n" +
                "    \"userName\": \"김건아\",\n" +
                "    \"docNo\": \"2024082120452245739\"\n" +
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

            // 인코딩된 응답 본문 출력
            String encodedResponseBody = response.getBody();
            System.out.println("Encoded Response Body: " + encodedResponseBody);

            // Content-Type 확인
            MediaType contentType = response.getHeaders().getContentType();
            System.out.println("Content-Type: " + contentType);

            // 응답이 URL 인코딩된 문자열로 전송된 경우, 이를 디코딩하여 JSON으로 변환
            if (encodedResponseBody != null && contentType != null && contentType.toString().contains("text/plain")) {
                // 1. ISO-8859-1로 인코딩된 데이터를 먼저 UTF-8로 변환
                String isoDecodedResponseBody = new String(encodedResponseBody.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                System.out.println("ISO Decoded Response Body: " + isoDecodedResponseBody);

                // 2. URL 디코딩
                String urlDecodedResponseBody = URLDecoder.decode(isoDecodedResponseBody, StandardCharsets.UTF_8.name());
                System.out.println("URL Decoded Response Body: " + urlDecodedResponseBody);

                // 3. JSON 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> parsedResponseBody = objectMapper.readValue(urlDecodedResponseBody, new TypeReference<Map<String, Object>>() {});

                // 디코딩 후 JSON 응답 검증
                assertThat(parsedResponseBody).containsKey("data");
                Map<String, Object> data = (Map<String, Object>) parsedResponseBody.get("data");

                // 원하는 필드 검증
                assertThat(data).containsKey("resIssueYN");
                assertThat(data.get("resIssueYN")).isInstanceOf(String.class);
                String resIssueYN = (String) data.get("resIssueYN");
                assertThat(resIssueYN).isNotEmpty();

                // 예시 출력
                System.out.println("Parsed JSON Data: " + data);

            } else {
                System.err.println("Unexpected content type or empty response: " + contentType);
            }

        } catch (Exception e) {
            // 오류 로그 출력
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // try {
        //     // API 엔드포인트로 POST 요청 전송
        //     ResponseEntity<String> response = restTemplate.exchange(
        //             API_URL,
        //             HttpMethod.POST,
        //             requestEntity,
        //             String.class
        //     );

        //     // 문자열 응답 본문 출력
        //     String responseBody = response.getBody();
        //     System.out.println("Response Body: " + responseBody);

        //     // Content-Type 확인
        //     MediaType contentType = response.getHeaders().getContentType();
        //     System.out.println("Content-Type: " + contentType);

        //     // 응답 검증
        //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        //     assertThat(responseBody).isNotNull();

        //     // JSON 파싱 시도 (Content-Type이 JSON일 경우만)
        //     if (contentType != null && contentType.equals(MediaType.APPLICATION_JSON)) {
        //         ObjectMapper objectMapper = new ObjectMapper();
        //         Map<String, Object> parsedResponseBody = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

        //         // 필드 검증
        //         assertThat(parsedResponseBody).containsKey("resIssueYN");
        //         assertThat(parsedResponseBody.get("resIssueYN")).isInstanceOf(String.class);
        //         String resIssueYN = (String) parsedResponseBody.get("resIssueYN");
        //         assertThat(resIssueYN).isNotEmpty();
        //     } else {
        //         System.err.println("Unexpected content type: " + contentType);
        //     }

        // } catch (Exception e) {
        //     // 오류 로그 출력
        //     System.err.println("Exception: " + e.getMessage());
        //     e.printStackTrace();
        //     throw e;
        // }
    }

    protected static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        try (BufferedReader br = null) {
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

    @org.springframework.context.annotation.Configuration
    static class TestConfig {
        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}


