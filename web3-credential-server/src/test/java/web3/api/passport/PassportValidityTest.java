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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;




@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PassportValidityTest {

    private RestTemplate restTemplate = new RestTemplate();

    private static final String ACCESS_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String API_URL = "https://development.codef.io/v1/kr/public/mw/passport-data/status";

    private static final String CLIENT_ID = "86640213-3b83-461a-97ab-2491d68a2052";
    private static final String CLIENT_SECRET = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";


    //codef api 계정의 public Key 입력하기
    private String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGXundpJlAHNhwDiVDSKWy4iJ+jzkawCMy3d1IZ0o5qHaOk8X2bVn9rL8lepioNNGcswWxhjs4UEqPGsu6+XPHbbYrUPNimlaa0dRsQcNdjD7flaSMIbDMeD5v04AZiquWcLZl1CqdzntLeYXVat7uqcQ68Sb5mGn0HYWN8XlQHpBMdmEESr0mJCEhLI2MD6+uqU8oMnrUnPJZSkKD83udCXjt1b0N8SksWBtWz3NQqsmx8a9NgYJlRSG1jkI8zgBzwtvnNxD4NaM/NqtDiuVXhNfupltzmA+xt4hy+DD00GKUcg05iRQih1go3WG8UKtA5KOqcfHS9e8S9z77lkkQIDAQAB"; //public key 넣기
    

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
        
        //String certFileEncoded = encodeFileToBase64(certFilePath);
        //String keyFileEncoded = encodeFileToBase64(keyFilePath);

/////////////////////////////////////////////////여기부터///////////////////////////////

        
        String certFileEncoded = "";
        String keyFileEncoded = "";

        // 사용자의 공동 인증서 비밀번호
        String certPassword = "";


        //사용자 실제값
        String userName = "";
        String identity= "";
        String passportNo= "";
        String issueDate = "";
        String expirationDate ="";
        String brithDate= "";

//////////////////////////////////////////////여기까지는 개인정보 유출 주의!!!/////////////////////

        String rsaEncryptedPassword = encryptRSAPassword(certPassword);

        // JSON Request Payload
        String requestBody = "{\n" +
                    //아래 3값(organization, loginType, certType)은 고정값
                "    \"organization\": \"0002\",\n" +
                "    \"loginType\": \"2\",\n" +
                "    \"certType\": \"1\",\n" +
                    //아래 10개 값은 사용자의 실제 데이터값 필요
                "    \"certFile\": \"" + certFileEncoded + "\",\n" +
                "    \"keyFile\": \"" + keyFileEncoded + "\",\n" +
                "    \"certPassword\": \"" + rsaEncryptedPassword + "\",\n" +
                "    \"userName1\": \""+userName+"\",\n" +
                "    \"identity\": \""+identity+"\",\n" + //주민등록번호

                "    \"userName\": \""+userName+"\",\n" +
                "    \"passportNo\": \""+passportNo+"\",\n" + //여권번호
                "    \"issueDate\": \""+issueDate+"\",\n" + //여권발급 날짜
                "    \"expirationDate\": \""+expirationDate+"\",\n" + //여권 만료 날짜
                "    \"birthDate\": \""+brithDate+"\"\n" + //생일
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

    public String encryptRSAPassword(String password) throws Exception {
        // 1. Base64로 인코딩된 공개키 문자열을 PublicKey 객체로 변환
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 2. Cipher 객체를 사용하여 RSA 암호화 수행
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // PKCS1Padding 사용
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // 3. 비밀번호를 암호화
        byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));

        // 4. 암호화된 바이트 배열을 Base64로 인코딩하여 반환
        return Base64.getEncoder().encodeToString(encryptedBytes);
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