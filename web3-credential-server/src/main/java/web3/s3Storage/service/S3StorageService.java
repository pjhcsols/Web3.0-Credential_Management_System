package web3.s3Storage.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.annotation.PreDestroy;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
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
import web3.domain.wallet.Wallet;
import web3.repository.wallet.WalletRepository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String bucketUrl;
    private final PDFMergerUtility merger;

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
        this.merger = new PDFMergerUtility();
    }

    public String uploadPdf(MultipartFile file, Wallet wallet,String pdfInfo) throws IOException {
        String fileName;
        byte[] result;
        HashMap<String, String> metadata = new HashMap<>();
        int nowPage = 1;

        //첫 등록일때 => 생성해줘야함
        if (wallet.getPdfUrl() == null){
            fileName = wallet.getAddress() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            System.out.println("fileName = " + fileName);
            // PDF 파일 확장자 검증
            validatePdfFile(fileName);
            result = file.getBytes();

        }
        else{
            //이미 있을시 -> pdf 병합
            String destination = wallet.getPdfUrl();
            fileName = extractKeyFromUrl(destination);
            System.out.println("fileName = " + fileName);

            byte[] first = getPdf(destination).readAllBytes();//원래 파일
            byte[] second = file.getBytes(); //뒤에 들어온 파일

            nowPage = getPdfPageCount(first)+1;
            result = mergePdfs(first, second);
            System.out.println("merge Success");


            metadata= getPdfMetadata(bucketName, fileName);
        }

        String metadataKey = "page-" + nowPage; // 키 설정
        metadata.put(metadataKey, pdfInfo); // 키-값 쌍으로 추가
        System.out.println("metadata = " + metadata);


        try {
            // S3에 PDF 파일 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .metadata(metadata) // 메타데이터 추가
                    .build();

            // PutObject 요청 수행
            s3Client.putObject(putRequest, RequestBody.fromBytes(result));

        } catch (S3Exception e) {
            throw new IOException("Failed to upload pdf to S3: " + e.getMessage());
        }
        wallet.updatePdfUrl(getpdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);
        return getpdfUrl(fileName);
    }

    public byte[] mergePdfs(byte[] pdf1, byte[] pdf2) throws IOException {

        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(pdf1);
        merger.addSource(inputStream1);

        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(pdf2);
        merger.addSource(inputStream2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);
        merger.mergeDocuments(null);

        return outputStream.toByteArray();
    }

    public HashMap<String, String> getPdfMetadata(String bucketName, String fileName) {
        HashMap<String, String> metadata;

        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build());

            metadata = new HashMap<>(response.metadata());

            if (metadata.isEmpty()) {
                System.out.println("No user-defined metadata found for this object.");
            }

        } catch (S3Exception e) {
            System.err.println("Failed to retrieve metadata: " + e.getMessage());
            return new HashMap<>();
        }

        return metadata;
    }




    public int getPdfPageCount(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return document.getNumberOfPages();
        }
    }

    private void validatePdfFile(String filename) {
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Illegal end name. Only Pdf.");
        }
    }


    //예외처리하기
    public void deletePdf(String urlToDelete) {
        String key = extractKeyFromUrl(urlToDelete);
        System.out.println("key = " + key);
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
        return url.substring(index+1);
    }

    private String getpdfUrl(String fileName) {
        return bucketUrl + "/" + fileName;
    }

    @PreDestroy
    public void cleanup() {
        s3Client.close();
    }

}