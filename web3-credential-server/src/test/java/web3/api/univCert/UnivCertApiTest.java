package web3.api.univCert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UnivCertApiTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @DisplayName("인증 코드 전송에 성공해야 합니다.")
    void shouldSendCertificationCodeSuccessfully() {
        // JSON Request Payload
        String jsonRequest = "{\n" +
                "  \"key\": \"43086994-92b9-4caa-8b3e-be2510051c8e\",\n" +
                "  \"email\": \"kga0416@knu.ac.kr\",\n" +
                "  \"univName\": \"경북대학교\",\n" +
                "  \"univ_check\": true\n" +
                "}";

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/certify",
                HttpMethod.POST,
                request,
                String.class
        );

        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());

        // Validate Response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    @DisplayName("인증 코드 검증에 성공해야 합니다.")
    void shouldVerifyCertificationCodeSuccessfully() {
        // JSON Request Payload
        String jsonRequest = "{\n" +
                "  \"key\": \"43086994-92b9-4caa-8b3e-be2510051c8e\",\n" +
                "  \"email\": \"kga0416@knu.ac.kr\",\n" +
                "  \"univName\": \"경북대학교\",\n" +
                "  \"code\": 8368\n" +
                "}";

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/certifycode",
                HttpMethod.POST,
                request,
                String.class
        );

        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());

        // Validate Response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }


    @Test
    @DisplayName("이메일 인증 상태를 성공적으로 조회해야 합니다.")
    void shouldGetEmailCertificationStatusSuccessfully() {
        // JSON Request Payload (성공 케이스)
        String jsonRequest = "{\n" +
                "  \"key\": \"43086994-92b9-4caa-8b3e-be2510051c8e\",\n" +  
                "  \"email\": \"kga0416@knu.ac.kr\"\n" +
                "}";
    
        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
    
        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/status",
                HttpMethod.POST,
                request,
                String.class
        );
    
        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());
    
        // Validate Response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    
        try {
            // Extract the 'certified_date' field if 'success' is true
            String certifiedDate = JsonPath.read(response.getBody(), "$.certified_date");
    
            // Validate that certified_date is not null
            assertThat(certifiedDate).isNotNull();  // certified_date가 존재하는지 확인
        } catch (PathNotFoundException e) {
            System.out.println("PathNotFoundException: " + e.getMessage());
            System.out.println("응답 본문에서 'certified_date' 필드를 찾지 못했습니다.");
        }
    }
    


    @Test
    @DisplayName("인증된 사용자 목록 조회에 성공해야 합니다.") // Test retrieving the list of certified users successfully
    void shouldGetCertifiedUserListSuccessfully() {
        // JSON 요청 페이로드
        String jsonRequest = "{\n" +
                "  \"key\": \"43086994-92b9-4caa-8b3e-be2510051c8e\"\n" +
                "}";

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity 생성
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // POST 요청 전송
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/certifiedlist",
                HttpMethod.POST,
                request,
                String.class
        );

        // 응답 로그 및 확인
        System.out.println("Response Body: " + response.getBody());

        // 응답 타입이 JSON인지 확인
        //assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        // 응답 상태 코드 및 본문 확인
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }


    @Test
    @DisplayName("인증된 사용자 목록 초기화에 성공해야 합니다.")
    void shouldClearCertifiedUserListSuccessfully() {
        // JSON Request Payload
        String jsonRequest = "{\n" +
                "  \"key\": \"43086994-92b9-4caa-8b3e-be2510051c8e\"\n" +
                "}";

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/clear",
                HttpMethod.POST,
                request,
                String.class
        );

        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());

        // Validate Response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    @DisplayName("인증 요청할 대학명 전송에 성공해야 합니다.")
    void shouldSendUnivNameSuccessfully() {
        // JSON Request Payload
        String jsonRequest = """
                {
                  "univName" : "경북대학교"
                }
                """;

        // Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity
        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

        // Send POST request to the /check endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "https://univcert.com/api/v1/check",
                HttpMethod.POST,
                request,
                String.class
        );

        // 로그에 응답 본문 출력
        System.out.println("Response Body: " + response.getBody());

        // Validate Response for success
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }
}
