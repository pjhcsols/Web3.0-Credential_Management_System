package web3.api.passport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PassportValidityTest {

    private RestTemplate restTemplate = new RestTemplate();

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

        // Load and encode the certFile and keyFile
        String certFilePath = "D:/NPKI/yessign/USER/cn=김건아()003104620200131131003160,ou=DGB,ou=personal4IB,o=yessign,c=kr/signCert.der";
        String keyFilePath = "D:/NPKI/yessign/USER/cn=김건아()003104620200131131003160,ou=DGB,ou=personal4IB,o=yessign,c=kr/signPri.key";
        
        String certFileEncoded = encodeFileToBase64(certFilePath);
        String keyFileEncoded = encodeFileToBase64(keyFilePath);
        
        String certPassword = "geonah2410!";  // 사용자의 인증서 비밀번호
        String rsaEncryptedPassword = encryptRSAPassword(certPassword);

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0002\",\n" +
                "    \"loginType\": \"0\",\n" +
                "    \"certType\": \"1\",\n" +
                "    \"certFile\": \"" + certFileEncoded + "\",\n" +
                "    \"keyFile\": \"" + keyFileEncoded + "\",\n" +
                "    \"certPassword\": \"" + rsaEncryptedPassword + "\",\n" +

                "    \"userName\": \"김건아\",\n" +
                "    \"passportNo\": \"M45554431\",\n" +
                "    \"issueDate\": \"201900729\",\n" +
                "    \"expirationDate\": \"20290729\",\n" +
                "    \"birthDate\": \"20010416\"\n" +
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

            // URL 인코딩된 응답 본문 디코딩
            String responseBody = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8);

            // 디코딩된 응답 본문 출력
            System.out.println("Decoded Response Body: " + responseBody);

            // Content-Type 확인
            MediaType contentType = response.getHeaders().getContentType();
            System.out.println("Content-Type: " + contentType);

            // 응답 검증
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseBody).isNotNull();

            // JSON 파싱 시도 (Content-Type이 JSON일 경우만)
            if (contentType != null && contentType.equals(MediaType.APPLICATION_JSON)) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> parsedResponseBody = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
                });

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

    // 파일을 Base64로 인코딩하는 메서드
    private String encodeFileToBase64(String filePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(fileContent);
    }

    // 비밀번호를 RSA로 암호화하는 메서드 (사용하는 인증서 제공기관의 암호화 방식에 맞게 수정 필요)
    private String encryptRSAPassword(String password) {
        // 실제 RSA 암호화는 사용자의 시스템 설정 및 제공된 공개키에 따라 다를 수 있습니다.
        // 이 부분은 사용자 환경에 맞게 구현해야 합니다.
        return password; // 여기서는 임시로 패스워드를 그대로 반환하지만, 실제로는 암호화된 값을 반환해야 합니다.
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
                    return mapper.readValue(responseStr.toString(), new TypeReference<HashMap<String, String>>() {
                    });
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
