package web3.controller.cert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import web3.service.cert.UnivCertService;

@RestController
@RequestMapping("/api/univcert")
public class UnivCertController {

    private final UnivCertService univCertService;

    public UnivCertController(UnivCertService univCertService) {
        this.univCertService = univCertService;
    }

    @Operation(summary = "대학교 인증 코드 전송", description = "대학 인증 코드를 이메일로 전송합니다.")
    @PostMapping("/send-code")
    public Mono<ResponseEntity<String>> sendCertificationCode(
            @Parameter(description = "사용자 이메일", required = true) @RequestParam String email,
            @Parameter(description = "대학교 이름", required = true) @RequestParam String univName) {
        return univCertService.sendCertificationRequest(email, univName)
                .map(ResponseEntity::ok) // Wrap the result in a ResponseEntity
                .onErrorReturn(ResponseEntity.badRequest().body("인증 코드 전송에 실패했습니다."));
    }

    @Operation(summary = "인증 코드 검증", description = "사용자가 받은 인증 코드를 검증합니다.")
    @PostMapping("/verify-code")
    public Mono<ResponseEntity<String>> verifyCertificationCode(
            @Parameter(description = "사용자 이메일", required = true) @RequestParam String email,
            @Parameter(description = "대학교 이름", required = true) @RequestParam String univName,
            @Parameter(description = "인증 코드", required = true) @RequestParam int code) {
        return univCertService.verifyCertificationCode(email, univName, code)
                .map(ResponseEntity::ok) // Wrap the result in a ResponseEntity
                .onErrorReturn(ResponseEntity.badRequest().body("인증 코드 검증에 실패했습니다."));
    }

    @Operation(summary = "인증된 사용자 목록 조회", description = "인증된 사용자의 목록을 반환합니다.")
    @GetMapping("/certified-list")
    public Mono<ResponseEntity<String>> getCertifiedUserList() {
        return univCertService.getCertifiedUserList()
                .map(ResponseEntity::ok) // Wrap the result in a ResponseEntity
                .onErrorReturn(ResponseEntity.badRequest().body("인증된 사용자 목록 조회에 실패했습니다."));
    }

    @Operation(summary = "인증된 사용자 목록 초기화", description = "인증된 사용자 목록을 초기화합니다.")
    @PostMapping("/clear-list")
    public Mono<ResponseEntity<String>> clearCertifiedUserList() {
        return univCertService.clearCertifiedUserList()
                .map(ResponseEntity::ok) // Wrap the result in a ResponseEntity
                .onErrorReturn(ResponseEntity.badRequest().body("인증된 사용자 목록 초기화에 실패했습니다."));
    }

    // Endpoint to check if a university name is certifiable
    @Operation(summary = "대학교 인증 가능 여부 체크", description = "대학교가 인증 가능한지 확인합니다.")
    @PostMapping("/check-univ")
    public Mono<ResponseEntity<String>> checkUniversity(
            @Parameter(description = "대학교 이름", required = true) @RequestParam String univName) {
        return univCertService.checkUniversity(univName)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body("대학교 인증 가능 여부 체크에 실패했습니다."));
    }

    // Endpoint to check if an email is already certified
    @Operation(summary = "이메일 인증 상태 체크", description = "특정 이메일이 이미 인증된 상태인지 확인합니다.")
    @PostMapping("/check-email-status")
    public Mono<ResponseEntity<String>> checkEmailStatus(
            @Parameter(description = "사용자 이메일", required = true) @RequestParam String email) {
        return univCertService.checkEmailStatus(email)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().body("이메일 인증 상태 체크에 실패했습니다."));
    }
}