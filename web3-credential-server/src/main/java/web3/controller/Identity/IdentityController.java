package web3.controller.Identity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web3.domain.wallet.Wallet;
import web3.exception.S3.S3UploadException;
import web3.service.Identity.IdentityService;
import web3.service.dto.Identity.PassportCertificationDto;
import web3.service.dto.Identity.StudentCertificationDto;
import web3.service.wallet.WalletService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/certifications")
@Slf4j
public class IdentityController {

    private final IdentityService identityService;
    private final WalletService walletService;
    @Autowired
    public IdentityController(IdentityService identityService, WalletService walletService) {
        this.identityService = identityService;
        this.walletService = walletService;
    }

    @Operation(summary = "재학증 pdf, 메타데이터 등록",description = "페이지에 pdf, 메타데이터 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> registerCertification(
            @RequestParam("file") MultipartFile file,
            @RequestParam("walletId") Long walletId,
            @RequestParam("email") String email,
            @RequestParam("univName") String univName,
            @RequestParam("univCheck") Boolean univCheck) {

        StudentCertificationDto certificationDto = new StudentCertificationDto(email, univName, univCheck, LocalDateTime.now());
        identityService.registerStudentCertification(walletId, certificationDto, file);

        return ResponseEntity.ok("재학증이 성공적으로 등록되었습니다.");
    }

    @PostMapping("/register-passport-certification")
    public ResponseEntity<String> registerPassportCertification(
            @RequestParam("file") MultipartFile file, //추가 등록
            @RequestParam("walletId") Long walletId,
            @RequestParam("certFile") MultipartFile certFile,
            @RequestParam("keyFile") MultipartFile keyFile,
            @RequestParam("certPassword") String certPassword, //직접입력
            @RequestParam("userName") String userName,
            @RequestParam("identity") String identity,
            @RequestParam("passportNo") String passportNo,
            @RequestParam("issueDate") String issueDate,
            @RequestParam("expirationDate") String expirationDate,
            @RequestParam("birthDate") String birthDate) {

        // PassportCertificationDto 생성, 파일을 Base64로 인코딩
        PassportCertificationDto passportCertificationDto = new PassportCertificationDto(
                PassportCertificationDto.encodeFileToBase64(certFile),
                PassportCertificationDto.encodeFileToBase64(keyFile),
                certPassword,
                userName,
                identity,
                passportNo,
                issueDate,
                expirationDate,
                birthDate,
                LocalDateTime.now() // 현재 시간을 직접 호출
        );
        // 여권 인증 등록
        identityService.registerPassportCertification(walletId, passportCertificationDto, file);

        return ResponseEntity.ok("여권 인증이 성공적으로 등록되었습니다.");
    }

    @Operation(summary = "PDF 대체하기", description = "지정된 페이지를 새 PDF 파일로 대체합니다.")
    @PostMapping("/replace-pdf")
    public ResponseEntity<String> replacePdf(
            @Parameter(description = "PDF 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "페이지 번호", required = true)
            @RequestParam("page") int page,
            @Parameter(description = "사용자 지갑 ID", required = true)
            @RequestParam("walletId") Long walletId,
            @Parameter(description = "인증서 이름", required = true)
            @RequestParam("certificateName") String certificateName) throws IOException {

        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet does not exist"));
        String certificateType = certificateName + "_" + walletId;
        // 인증서 타입에 따라 페이지를 교체
        String pdfUrl = identityService.replacePdfPage(wallet, certificateType, page, file);

        return ResponseEntity.ok(pdfUrl);
    }

    //주요로직
    @Operation(summary = "특정 wallet의 인증서 이름 목록 얻기", description = "주어진 walletId에 해당하는 지갑의 인증서 이름 목록을 반환합니다.")
    @GetMapping("/cert-names")
    public ResponseEntity<Set<String>> getCertNamesByWalletId(
            @Parameter(description = "지갑 ID", required = true)
            @RequestParam Long walletId) {
        Set<String> certNames = identityService.getCertNamesByWalletId(walletId);

        return ResponseEntity.ok().body(certNames);
    }

    @Operation(summary = "특정 지갑의 pdf의 특정 인증서 key 목록 얻기", description = "사용자의 등록된 인증서 이름 목록을 전체를 가져옵니다.")
    @GetMapping("/get-cert-names")
    public ResponseEntity<Set<String>> getCertNames(
            @Parameter(description = "pdf 파일 경로", required = true)
            @RequestParam String pdfUrl) {

        // 인증서 이름 목록 얻기
        Set<String> certNames = identityService.getCertNames(pdfUrl);

        return ResponseEntity.ok().body(certNames);
    }

    //"특정 wallet의 인증서 리스트 얻기"만들기 //walletId로 순회
    //주요로직
    @Operation(summary = "특정 wallet의 인증서 리스트 얻기", description = "주어진 walletId에 대한 인증서 리스트를 반환합니다.")
    @GetMapping("/wallet-list/certs")
    public ResponseEntity<HashMap<String, String>> getCertListByWalletId(
            @Parameter(description = "사용자 지갑 ID", required = true)
            @RequestParam Long walletId) {
        HashMap<String, String> certList = identityService.getCertListByWalletId(walletId);
        HashMap<String, String> decodedMetadata = identityService.decodeMetadata(certList);

        return ResponseEntity.ok().body(decodedMetadata);
    }


    @Operation(summary = "특정 인증서 리스트 얻기",description = "개인의 인증서{(key : value)..(key : value)}들을 모두 가져옵니다.")
    @GetMapping("/certs")
    public ResponseEntity<HashMap<String,String>> getCertList(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl) {
        HashMap<String, String> certList = identityService.getCertList(pdfUrl);
        HashMap<String, String> decodedMetadata = identityService.decodeMetadata(certList);

        return ResponseEntity.ok().body(decodedMetadata);

    }

    @Operation(summary = "특정 인증서의 value 얻기",description = "사용자의 인증서 목록중 원하는 인증서의 내용들을 가져옵니다.")
    @GetMapping("/get-content")
    public ResponseEntity<List<Map.Entry<String, String>>> getPdfKey(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam String pdfUrl,
            @Parameter(description = "인증서 이름",required = true)
            @RequestParam String certName,
            @Parameter(description = "지갑 ID",required = true)
            @RequestParam Long walletId) {

        List<Map.Entry<String, String>> contentsForCertName = identityService.getContentsForCertName(pdfUrl, certName, walletId);
        return ResponseEntity.ok().body(contentsForCertName);
    }

    @Operation(summary = "특정 인증서 리스트 얻기 - pdf 형식",description = "개인의 인증서들을 pdf의 형식으로 모두 가져옵니다.")
    @GetMapping("/get-pdf")
    public ResponseEntity<byte[]> getPdf(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl) {
        try {
            byte[] pdfData = identityService.getPdf(pdfUrl).readAllBytes();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "특정 인증서의 페이지 삭제", description = "지갑에서 특정 인증서를 삭제합니다. 즉, S3 스토리지에 pdf 파일에서 특정 페이지를 삭제합니다.")
    @PatchMapping("/delete-one")
    public ResponseEntity<Void> deleteCertForPage(
            @Parameter(description = "walletId", required = true)
            @RequestParam("walletId") Long walletId,
            @Parameter(description = "인증서 이름", required = true)
            @RequestParam("certificateName") String certificateName,
            @Parameter(description = "삭제할 페이지 번호", required = true)
            @RequestParam("page") int page // page 변수를 이곳에 선언
    ) throws IOException, S3UploadException {
        Wallet wallet = walletService.getWalletById(walletId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet does not exist"));
        String certificateType = certificateName + "_" + walletId;
        identityService.deletePdfForPage(wallet, certificateType, page);

        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "사용자 특정 인증서 삭제(지갑 삭제)",description = "모든 인증서를 삭제합니다. 즉,s3 스토리지에 사용자 pdf 파일을 모두 삭제합니다.")
    @DeleteMapping("/delete-pdf")
    public ResponseEntity<Void> deleteWallet(
            @Parameter(description = "지울 pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl,
            @RequestParam("walletId") Long walletId,
            @Parameter(description = "인증서 이름", required = true)
            @RequestParam("certificateName") String certificateName) {
        String certificateType = certificateName + "_" + walletId;
        identityService.deletePdf(pdfUrl, walletId, certificateType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 지갑의 모든 인증서 삭제", description = "지갑 ID에 해당하는 모든 인증서를 S3에서 삭제합니다.")
    @DeleteMapping("/delete-wallet-certificates")
    public ResponseEntity<Void> deleteWalletCertificates(
            @Parameter(description = "지갑 ID", required = true) @RequestParam("walletId") Long walletId) {
        identityService.deleteWalletCertificates(walletId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "특정 PDF 페이지 수 얻기", description = "특정 PDF의 총 페이지 수 반환")
    @GetMapping("/get-pdf-page-count")
    public ResponseEntity<Integer> getPdfPageCount(
            @Parameter(description = "PDF file URL", required = true)
            @RequestParam String pdfUrl) {
        int pageCount = identityService.getPdfPageCount(pdfUrl);
        return ResponseEntity.ok(pageCount);
    }

}
