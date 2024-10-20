package web3.controller.cert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import web3.service.cert.PassportService;
import web3.service.dto.cert.PassportRequestDto;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/passport")
public class PassportController {

    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @Operation(summary = "여권 유효성 검사", description = "여권의 유효성을 검사합니다.")
    @PostMapping("/check-validity")
    public Mono<ResponseEntity<String>> checkPassportValidity(
            @Parameter(description = "여권 요청 정보", required = true)
            @RequestParam("certFile") MultipartFile certFile,
            @RequestParam("keyFile") MultipartFile keyFile,
            @RequestParam("certPassword") String certPassword,
            @RequestParam("userName") String userName,
            @RequestParam("identity") String identity,
            @RequestParam("passportNo") String passportNo,
            @RequestParam("issueDate") String issueDate,
            @RequestParam("expirationDate") String expirationDate,
            @RequestParam("birthDate") String birthDate) {

        String certFileEncoded = encodeFileToBase64(certFile);
        String keyFileEncoded = encodeFileToBase64(keyFile);

        PassportRequestDto passportRequestDto = new PassportRequestDto(certFileEncoded, keyFileEncoded, certPassword, userName, identity, passportNo, issueDate, expirationDate, birthDate);

        return passportService.checkPassportValidity(passportRequestDto)
                .map(response -> ResponseEntity.ok("여권 유효성 검사 성공: " + response))
                .onErrorReturn(ResponseEntity.badRequest().body("여권 유효성 검사에 실패했습니다."));
    }

    private String encodeFileToBase64(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new RuntimeException("파일 인코딩 실패", e);
        }
    }

}
