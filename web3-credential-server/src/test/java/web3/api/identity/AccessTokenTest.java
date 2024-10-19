package web3.api.identity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AccessTokenTest {

    @Autowired
    private RestTemplate restTemplate;

    private static final String TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

    @Test
    void shouldPublishAccessTokenSuccessfully() throws Exception {
        HashMap<String, String> tokenResponse = publishToken(CLIENT_ID, CLIENT_SECRET);

        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.get("access_token")).isNotNull();

        System.out.println("Access Token: " + tokenResponse.get("access_token"));
    }

    protected static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        BufferedReader br = null;
        try {
            // HTTP 요청을 위한 URL 오브젝트 생성
            URL url = new URL(TOKEN_URL);
            String params = "grant_type=client_credentials&scope=read"; // Oauth2.0 사용자 자격증명 방식(client_credentials) 토큰 요청 설정

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 클라이언트아이디, 시크릿코드 Base64 인코딩
            String auth = clientId + ":" + clientSecret;
            String authStringEnc = Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + authStringEnc;

            con.setRequestProperty("Authorization", authHeader);
            con.setDoInput(true);
            con.setDoOutput(true);

            // 리퀘스트 바디 전송
            try (OutputStream os = con.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            // 응답 코드 확인
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 발생
                System.out.println("Failed to get access token: HTTP error code : " + responseCode);
                return null;
            }

            // 응답 바디 read
            StringBuilder responseStr = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                responseStr.append(inputLine);
            }

            // 응답결과 JSON Parsing
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
