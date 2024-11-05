package web3.api.qnet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class QnetTest {
    @Autowired
    private RestTemplate restTemplate;
    private static final String ACCESS_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String API_URL = "https://development.codef.io/v1/kr/etc/hr/qnet-certificate/status";

    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

    @Test
    void shouldCheckQnetCertificateStatusSuccessfully() throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
        // Get Access Token
        HashMap<String, String> tokenResponse = publishToken(CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenResponse).isNotNull();
        String accessToken = tokenResponse.get("access_token");
        assertThat(accessToken).isNotNull();

        // JSON Request Payload
        String requestBody = "{\n" +
                "    \"organization\": \"0001\",\n" +
                "    \"userName\": \"김건아\",\n" +
                "    \"docNo\": \"2024031002110534064\"\n" +
                "}";

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());

        // Validate Response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // URL 디코딩된 메시지 출력
        String decodedResponse = URLDecoder.decode(response.getBody(), "UTF-8");
        System.out.println("Decoded Response Body: " + decodedResponse);

        // JSON 파싱하여 필드 검증
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> responseMap = mapper.readValue(decodedResponse, new TypeReference<HashMap<String, Object>>() {});

        assertThat(responseMap).containsKey("result");
        HashMap<String, Object> resultMap = (HashMap<String, Object>) responseMap.get("result");
        assertThat(resultMap).containsKey("resIssueYN");
        assertThat(resultMap).containsKey("resDocType");
    }

    protected static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        BufferedReader br = null;
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
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                System.out.println("Failed to get access token: HTTP error code : " + responseCode);
                return null;
            }
            StringBuilder responseStr = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                responseStr.append(inputLine);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseStr.toString(), new TypeReference<HashMap<String, String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
