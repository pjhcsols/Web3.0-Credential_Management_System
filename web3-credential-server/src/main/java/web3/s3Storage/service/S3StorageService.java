package web3.s3Storage.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import web3.domain.user.User;
import web3.domain.wallet.Wallet;
import web3.repository.wallet.WalletRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String bucketUrl;

    private final WalletRepository walletRepository;


    @Autowired
    public S3StorageService(@Value("${cloud.aws.credentials.accessKey}") String accessKey,
                            @Value("${cloud.aws.credentials.secretKey}") String secretKey,
                            @Value("${cloud.aws.s3.bucket}") String bucketName,
                            @Value("${cloud.aws.s3.bucket.url}") String bucketUrl,
                            WalletRepository walletRepository) {
        this.s3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_2) // 원하는 리전을 지정해야 합니다.
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.bucketName = bucketName;
        this.bucketUrl = bucketUrl;
        this.walletRepository = walletRepository;
    }


    public String uploadPdf(MultipartFile file, Wallet wallet) throws IOException {
        String fileName = wallet.getAddress() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // PDF 파일 확장자 검증
        validatePdfFile(fileName);

        byte[] fileBytes = file.getBytes();

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(fileBytes));

        } catch (S3Exception e) {
            // S3 작업 중 예외 발생 시 처리
            throw new IOException("Failed to upload pdf to S3: " + e.getMessage());
        }

        wallet.setPdfUrl(getpdfUrl(fileName));
        return getpdfUrl(fileName);
    }

    private void validatePdfFile(String filename) {
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Illegal end name. Only Pdf.");
        }
    }


    //예외처리하기
    public void deletePdf(String urlToDelete) {
        String key = extractKeyFromUrl(urlToDelete);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.err.println("Failed to delete photo from S3: " + e.getMessage());
        }
    }

    public ResponseInputStream<GetObjectResponse> getPdf(String pdfUrl) {
        String key = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    private String extractKeyFromUrl(String url) {
        // Assuming the URL is in the format: https://s3.ap-northeast-2.amazonaws.com/bucketName/fileName
        int index = url.lastIndexOf('/');
        return url.substring(index + 1);
    }

    private String getpdfUrl(String fileName) {
        return bucketUrl + "/" + fileName;
    }

    @PreDestroy
    public void cleanup() {
        s3Client.close();
    }

}