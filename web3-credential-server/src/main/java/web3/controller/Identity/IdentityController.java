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
import web3.s3Storage.dto.DeleteCertRequest;
import web3.service.Identity.IdentityService;
import web3.service.dto.Identity.StudentCertificationDto;
import web3.service.wallet.WalletService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        StudentCertificationDto certificationDto = new StudentCertificationDto(email, univName, univCheck);

        identityService.registerStudentCertification(walletId, certificationDto, file);

        return ResponseEntity.ok("재학증이 성공적으로 등록되었습니다.");
    }

    @Operation(summary = "pdf 대체하기",description = "원하는 페이지를 원하는 pdf로 대체합니다.")
    @PostMapping("/replace-pdf")
    public ResponseEntity<String> replacePdf(
            @Parameter(description = "pdf 파일",required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "페이지 번호",required = true)
            @RequestParam("page") int page,
            @Parameter(description = "사용자 지갑ID",required = true)
            @RequestParam("walletId") Long walletId) throws IOException {
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        String pdfUrl = identityService.replacePdfPage(wallet, page, file);
        return ResponseEntity.ok(pdfUrl);
    }

    @Operation(summary = "사용자의 등록된 인증서 key 목록 전체 얻기", description = "사용자의 등록된 인증서 이름 목록을 전체를 가져옵니다.")
    @GetMapping("/get-cert-names")
    public ResponseEntity<Set<String>> getCertNames(
            @Parameter(description = "pdf 파일 경로", required = true)
            @RequestParam String pdfUrl) {

        // 인증서 이름 목록 얻기
        Set<String> certNames = identityService.getCertNames(pdfUrl);

        return ResponseEntity.ok().body(certNames);
    }

    @Operation(summary = "인증서 리스트 얻기",description = "개인의 인증서{(key : value)..(key : value)}들을 모두 가져옵니다.")
    @GetMapping("/certs")
    public ResponseEntity<HashMap<String,String>> getCertList(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl) {
        HashMap<String, String> certList = identityService.getCertList(pdfUrl);
        HashMap<String, String> decodedMetadata = identityService.decodeMetadata(certList);

        return ResponseEntity.ok().body(decodedMetadata);

    }

    @Operation(summary = "사용자의 해당되는 인증서의 특정 value 얻기",description = "사용자의 인증서 목록중 원하는 인증서의 내용들을 가져옵니다.")
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

    @Operation(summary = "인증서 리스트 얻기 - pdf 형식",description = "개인의 인증서들을 pdf의 형식으로 모두 가져옵니다.")
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

    @Operation(summary = "특정 인증서 삭제",description = "지갑에서 특정 인증서를 삭제합니다. 즉,S3 스토리지에 pdf 파일에서 특정 페이지를 삭제합니다.")
    @PatchMapping ("/delete-one")
    public ResponseEntity<Void> deleteCertForPage(
            @Parameter(description = "walletId와 삭제할 page가 담긴 Dto",required = true)
            @RequestBody DeleteCertRequest request
    ) throws IOException, S3UploadException {
        Long walletId = request.getWalletId();
        int page = request.getPage();
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        identityService.deletePdfForPage(wallet,page);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모든 인증서 삭제(지갑 삭제)",description = "모든 인증서를 삭제합니다. 즉,s3 스토리지에 pdf 파일을 모두 삭제합니다.")
    @DeleteMapping("/delete-pdf")
    public ResponseEntity<Void> deleteWallet(
            @Parameter(description = "지울 pdf 파일 경로 관련 Dto",required = true)
            @RequestParam("pdfUrl") String pdfUrl,
            @RequestParam("walletId") Long walletId) {
        identityService.deletePdf(pdfUrl, walletId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "PDF 페이지 수 얻기", description = "특정 PDF의 총 페이지 수 반환")
    @GetMapping("/get-pdf-page-count")
    public ResponseEntity<Integer> getPdfPageCount(
            @Parameter(description = "PDF file URL", required = true)
            @RequestParam String pdfUrl) {
        int pageCount = identityService.getPdfPageCount(pdfUrl);
        return ResponseEntity.ok(pageCount);
    }

}
