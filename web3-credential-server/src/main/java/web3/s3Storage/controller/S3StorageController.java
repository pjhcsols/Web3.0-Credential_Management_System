package web3.s3Storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web3.domain.wallet.Wallet;
import web3.s3Storage.dto.DeleteCertRequest;
import web3.s3Storage.dto.DeletePdfRequest;
import web3.s3Storage.service.S3StorageService;
import web3.service.wallet.WalletService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class S3StorageController {

    private final S3StorageService s3StorageService;
    private final WalletService walletService;

    @Autowired
    public S3StorageController(S3StorageService s3StorageService, WalletService walletService) {
        this.s3StorageService = s3StorageService;
        this.walletService = walletService;
    }

    @Operation(summary = "인증서(pdf) 업로드",description = "인증서를 업로드합니다. 즉,s3 스토리지에 pdf 파일을 업로드 합니다.")
    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(
            @Parameter(description = "업로드할 pdf 파일",required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "사용자 지갑ID",required = true)
            @RequestParam("walletId") Long walletId,
            @Parameter(description = "pdf 정보",required = true)
            @RequestParam String pdfInfo,
            @Parameter(description = "pdf 키값",required = true)
            @RequestParam String pdfKey) {
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        String pdfUrl = s3StorageService.uploadPdf(file, wallet,pdfInfo,pdfKey);
        return ResponseEntity.ok(pdfUrl);

    }


    @Operation(summary = "모든 인증서 삭제(지갑 삭제)",description = "모든 인증서를 삭제합니다. 즉,s3 스토리지에 pdf 파일을 모두 삭제합니다.")
    @DeleteMapping("/delete-pdf")
    public ResponseEntity<Void> deleteWallet(
            @Parameter(description = "지울 pdf 파일 경로 관련 Dto",required = true)
            @RequestBody DeletePdfRequest request) {
        String urlToDelete = request.getUrlToDelete();
        System.out.println("urlToDelete = " + urlToDelete);
        s3StorageService.deletePdf(urlToDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 인증서 삭제",description = "지갑에서 특정 인증서를 삭제합니다. 즉,S3 스토리지에 pdf 파일에서 특정 페이지를 삭제합니다.")
    @PatchMapping ("/delete-one")
    public ResponseEntity<Void> deleteCertForPage(
            @Parameter(description = "walletId와 삭제할 page가 담긴 Dto",required = true)
            @RequestBody DeleteCertRequest request
    ) throws IOException {
        System.out.println("request = " + request);
        Long walletId = request.getWalletId();
        int page = request.getPage();
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        s3StorageService.deletePdfForPage(wallet,page);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "인증서 리스트 얻기",description = "개인의 인증서들을 모두 가져옵니다. 형식 : (페이지 - 설명)")
    @GetMapping("/certs")
    public ResponseEntity<HashMap<String,String>> getCertList(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl) {
        HashMap<String, String> certList = s3StorageService.getCertList(pdfUrl);
        HashMap<String, String> decodedMetadata = s3StorageService.decodeMetadata(certList);
        //return ResponseEntity.ok().body(certList);
        return ResponseEntity.ok().body(decodedMetadata);

    }

    @Operation(summary = "인증서 리스트 얻기 - pdf 형식",description = "개인의 인증서들을 pdf의 형식으로 모두 가져옵니다.")
    @GetMapping("/get-pdf")
    public ResponseEntity<byte[]> getPdf(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam("pdfUrl") String pdfUrl) {
        try {
            byte[] pdfData = s3StorageService.getPdf(pdfUrl).readAllBytes();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "pdf 대체하기",description = "원하는 페이지를 원하는 pdf로 대체합니다.")
    @PostMapping("/replace-pdf")
    public ResponseEntity<String> replacePdf(
            @Parameter(description = "pdf 파일",required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "페이지 번호",required = true)
            @RequestParam("page") int page,
            @Parameter(description = "사용자 지갑ID",required = true)
            @RequestParam("walletId") Long walletId) {

        System.out.println("file = " + file);
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        String pdfUrl = s3StorageService.replacePdfPage(wallet, page, file);
        return ResponseEntity.ok(pdfUrl);

    }

    @Operation(summary = "메타데이터 얻기",description = "사용자의 모든 메타데이터의 정보들을 가져옵니다.")
    @GetMapping("/get-metadata")
    public ResponseEntity<String> getMetadata(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam String pdfUrl,
            @Parameter(description = "페이지 번호",required = true)
            @RequestParam int page) {
        String metadata = s3StorageService.getMetadataForPage(pdfUrl, page);

        return ResponseEntity.ok().body(metadata);
    }

    @Operation(summary = "pdf key값 얻기",description = "사용자의 pdf에서 원하는 페이지의 key값을 가져옵니다.")
    @GetMapping("/get-pdfkey")
    public ResponseEntity<String> getPdfKey(
            @Parameter(description = "pdf 파일 경로",required = true)
            @RequestParam String pdfUrl,
            @Parameter(description = "페이지 번호",required = true)
            @RequestParam int page) {
        String metadata = s3StorageService.getPdfKeyForPage(pdfUrl, page);
        return ResponseEntity.ok().body(metadata);
    }

}