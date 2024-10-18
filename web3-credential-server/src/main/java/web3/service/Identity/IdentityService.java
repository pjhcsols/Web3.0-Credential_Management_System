package web3.service.Identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import web3.domain.wallet.Wallet;
import web3.exception.S3.S3UploadException;
import web3.properties.S3Properties;
import web3.repository.wallet.WalletRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;


import web3.service.dto.Identity.StudentCertificationDto;

@Service
@Slf4j
public class IdentityService {
    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private final WalletRepository walletRepository;

    @Autowired
    public IdentityService(S3Properties s3Properties, WalletRepository walletRepository) {
        this.s3Properties = s3Properties;
        this.s3Client = s3Properties.getS3Client();
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void registerStudentCertification(Long walletId, StudentCertificationDto certificationDto, MultipartFile file) {
        // 지갑 조회
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        // 메타데이터 키 및 문자열 생성
        // 등록 및 인증시간 포함하기
        String metadataKey = "재학증_" + walletId;
        String metadataString = String.format("{\"email\":\"%s\",\"univName\":\"%s\",\"univ_check\":%b}",
                certificationDto.getEmail(),
                certificationDto.getUnivName(),
                certificationDto.isUnivCheck());

        String fileName = generatePdfFileName(walletId);
        byte[] result;

        try {
            // PDF 처리
            if (wallet.getPdfUrl() == null) {
                // 첫 등록일 때 => PDF 생성
                result = (file.getSize() > 0) ? getFileBytes(file) : createEmptyPdf();
            } else {
                // 이미 있을 시 -> PDF 병합
                String destination = wallet.getPdfUrl();
                byte[] first = getBytes(destination); // 원래 파일
                byte[] second = (file.getSize() > 0) ? getFileBytes(file) : createEmptyPdf(); // 새로 추가될 파일
                result = mergePdfs(first, second); // PDF 병합
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing file", e);
        }

        // 메타데이터 생성
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put(metadataKey, metadataString); // 메타데이터 추가

        log.info("metadata = {}", metadata);

        // S3에 업로드 및 지갑 업데이트
        uploadToS3(fileName, metadata, result);
        wallet.updatePdfUrl(getPdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);
    }

    // PDF 파일 이름 생성하는 메소드
    private String generatePdfFileName(Long walletId) {
        return walletId + "_certifications.pdf";
    }


    private byte[] getBytes(String destination) {
        byte[] first;
        try {
            first = getPdf(destination).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return first;
    }

    // 파일 바이트 배열을 가져오는 메서드
    private byte[] getFileBytes(MultipartFile file) throws IOException {
        return file.getBytes();
    }

    private void uploadToS3(String fileName, Map<String, String> metadata, byte[] result) {

        HashMap<String, String> encodedMetadata = new HashMap<>();
        for (String key : metadata.keySet()) {
            encodedMetadata.put(URLEncoder.encode(key,StandardCharsets.UTF_8), URLEncoder.encode(metadata.get(key), StandardCharsets.UTF_8));
        }

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .contentType("application/pdf")
                .metadata(encodedMetadata)
                .build();
        try {
            s3Client.putObject(putRequest, RequestBody.fromBytes(result));
        } catch (S3Exception e) {
            System.err.println("S3 upload failed: " + e.awsErrorDetails().errorMessage());
        }
    }

    public byte[] mergePdfs(byte[] pdf1, byte[] pdf2){
        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(pdf1);
        merger.addSource(inputStream1);

        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(pdf2);
        merger.addSource(inputStream2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);
        try {
            merger.mergeDocuments(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    private byte[] createEmptyPdf(){
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage()); // 빈 페이지 추가
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String replacePdfPage(Wallet wallet, int pageNumberToRemove, MultipartFile newPdfFile) throws IOException{
        // 원래 PDF 가져오기
        String pdfUrl = wallet.getPdfUrl();
        byte[] originalPdfBytes = getOriginalPdfBytes(pdfUrl);
        String fileName = extractKeyFromUrl(pdfUrl);

        // 기존 PDF 로드와 리소스 관리
        PDDocument originalDocument = loadOriginalDocument(originalPdfBytes);

        int totalPages = originalDocument.getNumberOfPages();

        // 페이지 번호는 0부터 시작하므로 1을 빼줌
        int pageIndexToRemove = pageNumberToRemove - 1;

        // 페이지가 존재하는지 확인
        if (pageIndexToRemove < 0 || pageIndexToRemove >= totalPages) {
            throw new IllegalArgumentException("Page number out of range: " + pageNumberToRemove);
        }

        // 새로운 PDF 파일 로드
        byte[] newPdfBytes = getFileBytes(newPdfFile);

        // 앞부분과 뒷부분 PDF 바이트 배열 생성
        byte[] frontPart = createPdfBytesPart(originalDocument, 0, pageIndexToRemove);
        byte[] backPart = createPdfBytesPart(originalDocument, pageIndexToRemove + 1, originalDocument.getNumberOfPages());

        // PDF 합치기
        byte[] finalPdfBytes = mergeThreePdfs(frontPart, newPdfBytes, backPart);
        HashMap<String, String> metadata = decodeMetadata(getPdfMetadata(fileName));

        // 최종 PDF를 S3에 업로드
        uploadToS3(fileName, metadata, finalPdfBytes);

        try {
            originalDocument.close(); // 리소스 닫기
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pdfUrl; // 최종 PDF URL 반환
    }

    // PDF 문서를 로드하는 메서드 (예외 처리 포함)
    private PDDocument loadOriginalDocument(byte[] originalPdfBytes) {
        try {
            return PDDocument.load(new ByteArrayInputStream(originalPdfBytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load original PDF: " + e.getMessage(), e);
        }
    }

    private byte[] getOriginalPdfBytes(String pdfUrl) {
        byte[] originalPdfBytes;
        try {
            originalPdfBytes = getPdf(pdfUrl).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return originalPdfBytes;
    }


    private byte[] createPdfBytesPart(PDDocument document, int startIndex, int endIndex) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PDDocument partDocument = new PDDocument();

        // 지정된 인덱스 범위에 따라 페이지 추가
        for (int i = startIndex; i < endIndex; i++) {
            PDPage page = document.getPage(i);
            partDocument.addPage(page);
        }

        try {
            partDocument.save(outputStream);
            partDocument.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }


    // 세 개의 PDF 바이트 배열을 합치는 메서드
    private byte[] mergeThreePdfs(byte[] pdf1, byte[] pdf2, byte[] pdf3){
        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(pdf1);
        merger.addSource(inputStream1);

        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(pdf2);
        merger.addSource(inputStream2);

        ByteArrayInputStream inputStream3 = new ByteArrayInputStream(pdf3);
        merger.addSource(inputStream3);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);
        try {
            merger.mergeDocuments(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray(); // 최종 합쳐진 PDF 바이트 배열 반환
    }

    @Transactional
    //인증서 리스트 반환
    public HashMap<String, String> getCertList(String pdfUrl) {
        HashMap<String, String> metadata = getPdfMetadata(pdfUrl);
        HashMap<String, String> certList = new HashMap<>();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            certList.put(key, value);
        }

        return certList;
    }

    @Transactional
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
                throw new RuntimeException("No user-defined metadata found for this object.");
            }

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to retrieve metadata: " + e.getMessage());
        }

        return metadata;
    }

    @Transactional
    public Set<String> getCertNames(String pdfUrl) {
        String fileName = extractKeyFromUrl(pdfUrl);

        // S3에서 객체 가져오기
        GetObjectRequest getRequest = getGetObjectRequest(fileName);
        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();

        // 메타데이터 디코딩
        Map<String, String> metadata = decodeMetadata(getObjectResponse.metadata());

        // 접두사를 저장할 Set
        Set<String> certNames = new HashSet<>();

        // 메타데이터의 키를 순회하며 접두사 추출
        for (String key : metadata.keySet()) {
            if (key.contains("_")) {
                String certName = key.split("_")[0]; // '_'로 분리하여 첫 번째 요소 추출
                certNames.add(certName); // Set에 추가하여 중복 제거
            }
        }

        return certNames; // Set 형태로 반환
    }


    public List<Map.Entry<String, String>> getContentsForCertName(String pdfUrl, String certName, Long walletId) {
        String fileName = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getRequest = getGetObjectRequest(fileName);
        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();
        String value = null;
        List<Map.Entry<String, String>> tuples = new ArrayList<>();

        // 메타데이터 디코딩
        Map<String, String> metadata = decodeMetadata(getObjectResponse.metadata());

        // 주어진 certName과 walletId에 해당하는 메타데이터 찾기
        for (String key : metadata.keySet()) {
            if (key.startsWith(certName + "_") && key.endsWith(walletId.toString())) {
                value = metadata.get(key);
                break;
            }
        }

        // value 값이 없는 경우 null 처리
        if (value == null) {
            return tuples; // 빈 리스트 반환
        }

        // JSON 파싱을 위해 ObjectMapper 사용
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 형식으로 저장된 value를 Map으로 변환
            Map<String, Object> parsedJson = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});

            // Map의 각 항목을 (key, value) 형태로 변환하여 리스트에 추가
            for (Map.Entry<String, Object> entry : parsedJson.entrySet()) {
                tuples.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 오류 발생 시 빈 리스트 반환
            return tuples;
        }

        System.out.println("tuples = " + tuples);
        return tuples;
    }

    private GetObjectRequest getGetObjectRequest(String fileName) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .build();
        return getRequest;
    }

    @Transactional // 트랜잭션 관리
    public void deletePdf(String urlToDelete, Long walletId) {
        String key = extractKeyFromUrl(urlToDelete);

        try {
            // S3에서 PDF 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getS3BucketName())
                    .key(key)
                    .build());

            // Wallet 객체를 DB에서 가져오기 (영속성 컨텍스트에서 관리)
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            // Wallet의 pdfUrl 값을 빈 문자열로 변경
            wallet.updatePdfUrl(null); // pdfUrl을 빈 문자열로 설정

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete photo from S3: " + e.getMessage());
        }
    }


    @Transactional
    public void deletePdfForPage(Wallet wallet, int pageNumberToRemove) throws IOException, S3UploadException {
        String pdfUrl = wallet.getPdfUrl();
        byte[] originalPdfBytes = getPdf(pdfUrl).readAllBytes();
        String fileName = extractKeyFromUrl(pdfUrl);

        // 기존 PDF 로드와 리소스 관리
        try (PDDocument originalDocument = PDDocument.load(new ByteArrayInputStream(originalPdfBytes))) {
            int totalPages = originalDocument.getNumberOfPages();

            // 페이지 번호는 0부터 시작하므로 1을 빼줌
            int pageIndexToRemove = pageNumberToRemove - 1;

            // 페이지가 존재하는지 확인
            checkPageExist(pageIndexToRemove < 0, pageIndexToRemove >= totalPages, "Page number out of range: " + pageNumberToRemove);

            byte[] frontPart = createPdfBytesPart(originalDocument, 0, pageIndexToRemove);
            byte[] backPart = createPdfBytesPart(originalDocument, pageIndexToRemove + 1, totalPages);

            // PDF 합치기
            byte[] finalPdfBytes = mergePdfs(frontPart, backPart);

            // 기존 메타데이터를 그대로 가져옴
            HashMap<String, String> metadata = getPdfMetadata(fileName);

            // 최종 PDF를 S3에 업로드 (메타데이터는 수정하지 않음)
            try {
                uploadToS3(fileName, metadata, finalPdfBytes); // 기존 메타데이터를 그대로 사용
            } catch (S3Exception e) {
                throw new S3UploadException("Failed to upload pdf to S3: " + e.getMessage());
            }
        }
    }


    private void checkPageExist(boolean pageIndexToRemove, boolean pageIndexToRemove1, String pageNumberToRemove) {
        if (pageIndexToRemove || pageIndexToRemove1) {
            throw new IllegalArgumentException(pageNumberToRemove);
        }
    }

    // 메타 데이터 디코딩 메서드
    public LinkedHashMap<String, String> decodeMetadata(Map<String, String> metadata) {
        LinkedHashMap<String, String> decodedMetadata = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = URLDecoder.decode(entry.getKey(),StandardCharsets.UTF_8);
            // URL 디코딩
            String value = URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8);
            decodedMetadata.put(key, value);
        }
        return decodedMetadata;
    }

    public ResponseInputStream<GetObjectResponse> getPdf(String pdfUrl) {
        String key = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getObjectRequest = getGetObjectRequest(key);
        return s3Client.getObject(getObjectRequest);
    }

    private String extractKeyFromUrl(String url) {
        //예상 포맷: https://s3.ap-northeast-2.amazonaws.com/bucketName/fileName
        int index = url.lastIndexOf('/');
        return url.substring(index+1);
    }

    private String getPdfUrl(String fileName) {
        return s3Properties.getS3BucketUrl() + "/" + fileName;
    }

    @PreDestroy
    public void cleanup() {
        s3Client.close();
    }


    public int getPdfPageCount(String pdfUrl) {
        byte[] pdfBytes = getOriginalPdfBytes(pdfUrl); // PDF URL로부터 바이트 배열 가져오기

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF document: " + e.getMessage());
        }
    }


}