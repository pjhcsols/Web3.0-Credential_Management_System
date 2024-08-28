package web3.s3Storage.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web3.domain.wallet.Wallet;
import web3.s3Storage.dto.DeletePdfRequest;
import web3.s3Storage.service.S3StorageService;
import web3.service.wallet.WalletService;

import java.io.IOException;
import java.util.HashMap;

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

    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file,
                                            @RequestParam("walletId") Long walletId,
                                            @RequestParam String pdfInfo,
                                            @RequestParam String pdfKey) {
        Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
        try {
            String pdfUrl = s3StorageService.uploadPdf(file, wallet,pdfInfo,pdfKey);
            return ResponseEntity.ok(pdfUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload pdf: " + e.getMessage());
        }
    }


    @DeleteMapping("/delete-pdf")
    public ResponseEntity<Void> deletePdf(@RequestBody DeletePdfRequest request) {
        String urlToDelete = request.getUrlToDelete();
        System.out.println("urlToDelete = " + urlToDelete);
        s3StorageService.deletePdf(urlToDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/certs")
    public ResponseEntity<HashMap<String,String>> getCertList(@RequestParam("pdfUrl") String pdfUrl) {
        HashMap<String, String> certList = s3StorageService.getCertList(pdfUrl);
        return ResponseEntity.ok().body(certList);

    }

    @GetMapping("/get-pdf")
    public ResponseEntity<byte[]> getPdf(@RequestParam("pdfUrl") String pdfUrl) {
        try {
            System.out.println("pdfUrl = " + pdfUrl);
            byte[] pdfData = s3StorageService.getPdf(pdfUrl).readAllBytes();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/replace-pdf")
    public ResponseEntity<String> replacePdf(@RequestParam("file") MultipartFile file,
                                             @RequestParam("page") int page,
                                             @RequestParam("walletId") Long walletId) {
        try {
            System.out.println("file = " + file);
            Wallet wallet = walletService.getWalletById(walletId).orElseThrow(()-> new EntityNotFoundException("Wallet does not exist"));
            String pdfUrl = s3StorageService.replacePdfPage(wallet, page, file);
            return ResponseEntity.ok(pdfUrl);
        }catch(IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to replace pdf: " + e.getMessage());
        }
    }

    @GetMapping("/get-metadata")
    public ResponseEntity<String> getMetadata(@RequestParam String pdfUrl,
                                              @RequestParam int page) {
        String metadata = s3StorageService.getMetadataForPage(pdfUrl, page);
        return ResponseEntity.ok().body(metadata);
    }



    /*//스케줄러 용
    @GetMapping("/all/photourl")
    public ResponseEntity<List<String>> getAllImageUrls() {
        List<String> imageUrls = s3StorageService.getAllImageUrls();
        return ResponseEntity.ok(imageUrls);
    }*/

}