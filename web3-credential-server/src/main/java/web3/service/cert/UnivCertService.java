package web3.service.cert;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UnivCertService {

    private final WebClient webClient;
    private static final String CERT_KEY = "43086994-92b9-4caa-8b3e-be2510051c8e";
    private static final String BASE_URL = "https://univcert.com/";

    public UnivCertService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    // 인증 코드 전송 메서드
    public Mono<String> sendCertificationRequest(String email, String univName) {
        String jsonRequest = "{\n" +
                "  \"key\": \"" + CERT_KEY + "\",\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"univName\": \"" + univName + "\",\n" +
                "  \"univ_check\": true\n" +
                "}";

        return makePostRequest("api/v1/certify", jsonRequest);
    }

    // 인증 코드 검증 메서드
    // 검증 완료되면 사용자 초기화 후 지갑 생성 후 지갑 id를 포함해서 재학증_walletId_0을 저장한다
    // 재학증을 새로 등록 할때마다 -> 재학증_walletId_0 -> 재학증_walletId_1 -> 재학증_walletId_2 처럼 +1씩 증가되어서 블록에 키로 저장됨
    public Mono<String> verifyCertificationCode(String email, String univName, int code) {
        String jsonRequest = "{\n" +
                "  \"key\": \"" + CERT_KEY + "\",\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"univName\": \"" + univName + "\",\n" +
                "  \"code\": " + code + "\n" +
                "}";

        return makePostRequest("api/v1/certifycode", jsonRequest);
    }

    // 인증된 사용자 목록 조회 메서드
    public Mono<String> getCertifiedUserList() {
        String jsonRequest = "{\n" +
                "  \"key\": \"" + CERT_KEY + "\"\n" +
                "}";

        return makePostRequest("api/v1/certifiedlist", jsonRequest);
    }

    // 인증된 사용자 목록 초기화 메서드
    public Mono<String> clearCertifiedUserList() {
        String jsonRequest = "{\n" +
                "  \"key\": \"" + CERT_KEY + "\"\n" +
                "}";

        return makePostRequest("api/v1/clear", jsonRequest);
    }

    // 인증 가능한 대학명 체크 메서드
    public Mono<String> checkUniversity(String univName) {
        String jsonRequest = "{\n" +
                "  \"univName\": \"" + univName + "\"\n" +
                "}";

        return makePostRequest("api/v1/check", jsonRequest);
    }

    // 이메일 인증 상태 체크 메서드
    public Mono<String> checkEmailStatus(String email) {
        String jsonRequest = "{\n" +
                "  \"key\": \"" + CERT_KEY + "\",\n" +
                "  \"email\": \"" + email + "\"\n" +
                "}";

        return makePostRequest("api/v1/status", jsonRequest);
    }

    // HTTP POST 요청을 위한 헬퍼 메서드
    private Mono<String> makePostRequest(String endpoint, String jsonRequest) {
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonRequest)
                .retrieve()
                .bodyToMono(String.class);
    }
}
