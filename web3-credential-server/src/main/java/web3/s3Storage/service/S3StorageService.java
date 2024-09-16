package web3.s3Storage.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import web3.domain.wallet.Wallet;
import web3.properties.S3Properties;
import web3.repository.wallet.WalletRepository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class S3StorageService {

    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private final WalletRepository walletRepository;

    public S3StorageService(S3Properties s3Properties, WalletRepository walletRepository) {
        this.s3Properties = s3Properties;
        this.s3Client = s3Properties.getS3Client();
        this.walletRepository = walletRepository;
    }

    public String uploadPdf(MultipartFile file, Wallet wallet,String pdfInfo,String pdfKey) throws IOException {
        String fileName;
        byte[] result;
        HashMap<String, String> metadata = new HashMap<>();
        int nowPage = 1;

        //첫 등록일때 => 생성해줘야함
        if (wallet.getPdfUrl() == null){
            log.info("file = {}", file);

            fileName = (file.getSize() > 0) ? getFileName(file, wallet): getEmptyFilename(wallet);

            log.info("fileName = {}", fileName);

            // PDF 파일 확장자 검증
            //validatePdfFile(fileName);
            result = (file.getSize() > 0) ? file.getBytes():createEmptyPdf();

        }
        else{
            //이미 있을시 -> pdf 병합
            String destination = wallet.getPdfUrl();
            fileName = extractKeyFromUrl(destination);
            log.info("destination = {}", destination);
            log.info("fileName = {}", fileName);

            byte[] first = getPdf(destination).readAllBytes();//원래 파일
            byte[] second = (file.getSize() > 0) ? file.getBytes() : createEmptyPdf(); //뒤에 들어온 파일

            nowPage = getPdfPageCount(first)+1;
            result = mergePdfs(first, second);
            log.info("merge Success");


            metadata= getPdfMetadata(fileName);
        }

        String page = "page-" + nowPage; // 키 설정
        String value = pdfInfo + ":" + pdfKey;
        metadata.put(page,value); // 키-값 쌍으로 추가
        log.info("metadata = {}", metadata);

        try {
            // S3에 PDF 파일 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getS3BucketName())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(result));

        } catch (S3Exception e) {
            throw new IOException("Failed to upload pdf to S3: " + e.getMessage());
        }

        wallet.updatePdfUrl(getpdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);

        return getpdfUrl(fileName);
    }

    private static String getFileName(MultipartFile file, Wallet wallet) {
        return wallet.getAddress() + "_" + System.currentTimeMillis() + "_" + wallet.getAddress();
    }

    private static String getEmptyFilename(Wallet wallet) {
        return wallet.getAddress() + "_" + System.currentTimeMillis() + "_" + "empty.pdf";
    }

    public byte[] mergePdfs(byte[] pdf1, byte[] pdf2) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(pdf1);
        merger.addSource(inputStream1);

        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(pdf2);
        merger.addSource(inputStream2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);
        merger.mergeDocuments(null);

        return outputStream.toByteArray();
    }

    private byte[] createEmptyPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage()); // 빈 페이지 추가
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public String replacePdfPage(Wallet wallet, int pageNumberToRemove, MultipartFile newPdfFile) throws IOException {
        // 원래 PDF 가져오기
        String pdfUrl = wallet.getPdfUrl();
        byte[] originalPdfBytes = getPdf(pdfUrl).readAllBytes();
        String fileName = extractKeyFromUrl(pdfUrl);

        // 기존 PDF 로드
        PDDocument originalDocument = PDDocument.load(new ByteArrayInputStream(originalPdfBytes));
        int totalPages = originalDocument.getNumberOfPages();

        // 페이지 번호는 0부터 시작하므로 1을 빼줌
        int pageIndexToRemove = pageNumberToRemove - 1;

        // 페이지가 존재하는지 확인
        if (pageIndexToRemove < 0 || pageIndexToRemove >= totalPages) {
            throw new IllegalArgumentException("Page number out of range: " + pageNumberToRemove);
        }

        // 새로운 PDF 파일 로드
        byte[] newPdfBytes = newPdfFile.getInputStream().readAllBytes();

        // 앞부분과 뒷부분 PDF 바이트 배열 생성
        byte[] frontPart = createPdfBytesFrontPart(originalDocument, pageIndexToRemove);
        byte[] backPart = createPdfBytesBackPart(originalDocument, pageIndexToRemove);

        // PDF 합치기
        byte[] finalPdfBytes = mergeThreePdfs(frontPart, newPdfBytes, backPart);
        HashMap<String, String> metadata = getPdfMetadata(fileName);

        // 최종 PDF를 S3에 업로드
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getS3BucketName())
                    .key(fileName)
                    .contentType(newPdfFile.getContentType())
                    .metadata(metadata)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(finalPdfBytes));

        } catch (S3Exception e) {
            throw new IOException("Failed to upload pdf to S3: " + e.getMessage());
        } finally {
            originalDocument.close();
        }

        return pdfUrl; // 최종 PDF 바이트 배열 반환
    }

    // 앞부분 PDF 바이트 배열 생성
    private byte[] createPdfBytesFrontPart(PDDocument document, int pageIndexToRemove) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PDDocument frontPart = new PDDocument();

        // 앞부분 (제거할 페이지 이전까지)
        for (int i = 0; i < pageIndexToRemove; i++) {
            PDPage page = document.getPage(i);
            frontPart.addPage(page);
        }

        frontPart.save(outputStream); // 앞부분 PDF 저장
        frontPart.close();
        return outputStream.toByteArray();
    }

    // 뒷부분 PDF 바이트 배열 생성
    private byte[] createPdfBytesBackPart(PDDocument document, int pageIndexToRemove) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PDDocument backPart = new PDDocument();

        // 뒷부분 (제거할 페이지 이후부터)
        for (int i = pageIndexToRemove + 1; i < document.getNumberOfPages(); i++) {
            PDPage page = document.getPage(i);
            backPart.addPage(page);
        }

        backPart.save(outputStream);
        backPart.close();
        return outputStream.toByteArray();
    }

    // 세 개의 PDF 바이트 배열을 합치는 메서드
    public byte[] mergeThreePdfs(byte[] pdf1, byte[] pdf2, byte[] pdf3) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(pdf1);
        merger.addSource(inputStream1);

        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(pdf2);
        merger.addSource(inputStream2);

        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(pdf3);
        merger.addSource(inputStream3);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);
        merger.mergeDocuments(null);

        return outputStream.toByteArray(); // 최종 합쳐진 PDF 바이트 배열 반환
    }

    //인증서 리스트 반환
    public HashMap<String, String> getCertList(String pdfUrl) {
        HashMap<String, String> metadata = getPdfMetadata(pdfUrl);
        HashMap<String, String> certList = new HashMap<>();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null && value.contains(":")) {
                String extractedValue = value.split(":")[0];

                if (key.startsWith("page-")) {
                    String pageNumber = key.substring("page-".length());
                    certList.put(pageNumber, extractedValue);
                } else {
                    certList.put(key, extractedValue);
                }
            } else {
                certList.put(key, value);
            }
        }

        return certList;
    }



    public HashMap<String, String> getPdfMetadata( String pdfUrl) {
        HashMap<String, String> metadata;
        String fileName = extractKeyFromUrl(pdfUrl);

        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(s3Properties.getS3BucketName())
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

    public String getMetadataForPage(String pdfUrl, int pageNumber) {
        System.out.println("pdfUrl = " + pdfUrl);
        String fileName = extractKeyFromUrl(pdfUrl);

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .build();

        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();

        Map<String, String> metadata = getObjectResponse.metadata();

        String pageKey = "page-" + pageNumber;

        String result = metadata.get(pageKey);

        // 내용 부분만 추출
        if (result != null && result.contains(":")) {
            return result.split(":")[0]; // ':'기준으로 분리
        }

        return null; // 결과가 없거나 ':'가 없는 경우 null 반환
    }

    public String getPdfKeyForPage(String pdfUrl, int pageNumber) {
        String fileName = extractKeyFromUrl(pdfUrl);

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .build();

        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();

        Map<String, String> metadata = getObjectResponse.metadata();

        String pageKey = "page-" + pageNumber;

        String result = metadata.get(pageKey);

        // pdfKey 부분만 추출
        if (result != null && result.contains(":")) {
            return result.split(":")[1]; // ':'기준으로 분리
        }

        return null; // 결과가 없거나 ':'가 없는 경우 null 반환
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
                    .bucket(s3Properties.getS3BucketName())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.err.println("Failed to delete photo from S3: " + e.getMessage());
        }
    }

    public ResponseInputStream<GetObjectResponse> getPdf(String pdfUrl) {
        String key = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public String getPageByKey(HashMap<String, String> metadata, String key) {
        return metadata.get(key);
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
        return s3Properties.getS3BucketUrl() + "/" + fileName;
    }

    @PreDestroy
    public void cleanup() {
        s3Client.close();
    }

}