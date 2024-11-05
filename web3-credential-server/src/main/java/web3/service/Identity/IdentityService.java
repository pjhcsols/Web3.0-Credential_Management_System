package web3.service.Identity;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import web3.properties.RsaProperties;
import web3.properties.S3Properties;
import web3.repository.wallet.WalletRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import web3.service.dto.Identity.PassportCertificationDto;
import web3.service.dto.Identity.StudentCertificationDto;

import javax.crypto.Cipher;

@Service
@Slf4j
public class IdentityService {
    private final S3Properties s3Properties;
    private final RsaProperties rsaProperties;
    private final S3Client s3Client;
    private final WalletRepository walletRepository;

    @Autowired
    public IdentityService(S3Properties s3Properties, WalletRepository walletRepository, RsaProperties rsaProperties) {
        this.s3Properties = s3Properties;
        this.s3Client = s3Properties.getS3Client();
        this.walletRepository = walletRepository;
        this.rsaProperties = rsaProperties;
    }

    // RSA 암호화 로직 메서드
    private String encryptMetadata(String metadata, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(metadata.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // RSA 복호화 로직 메서드
    private String decryptMetadata(String encryptedMetadata, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMetadata));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Transactional
    public void registerStudentCertification(Long walletId, StudentCertificationDto certificationDto, MultipartFile file) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        String metadataKey = "재학증_" + walletId;
        String metadataString = String.format("{\"email\":\"%s\",\"univName\":\"%s\",\"univ_check\":%b,\"certified_date\":\"%s\"}",
                certificationDto.getEmail(),
                certificationDto.getUnivName(),
                certificationDto.isUnivCheck(),
                certificationDto.getCertifiedDate());

        String fileName = generateStudentPdfFileName(walletId);
        byte[] result;

        try {
            String destination = wallet.getPdfUrls().get(metadataKey);
            result = handlePdfProcessing(destination, file); //pdf 없으면 생성
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류 발생", e);
        }

        // RSA 암호화 및 복호화 키 설정
        PublicKey publicKey = wallet.getPublicKeyDecoder();  // 공개키 객체로 변환

        String encryptedMetadata;
        // 공개키로 암호화
        try {
            encryptedMetadata = encryptMetadata(metadataString, publicKey);
        } catch (Exception e) {
            throw new RuntimeException("메타데이터 암호화 중 오류 발생", e);
        }

        HashMap<String, String> metadata = new HashMap<>();

        metadata.put(metadataKey, encryptedMetadata);

        log.info("metadata = {}", metadata);
        uploadToS3(fileName, metadata, result);

        // PDF 파일 해시값 생성
        String pdfHash = generatePdfHash(result);
        // 지갑에 해시값 추가
        wallet.updatePdfHash(metadataKey, pdfHash);
        wallet.updatePdfUrl(metadataKey, getPdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);
    }

    // PDF 파일 이름 생성하는 메소드
    private String generateStudentPdfFileName(Long walletId) {
        return walletId + "_student_certifications.pdf";
    }

    @Transactional
    public void registerPassportCertification(Long walletId, PassportCertificationDto certificationDto, MultipartFile file) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        String metadataKey = "여권_" + walletId;

        String metadataString = String.format("{\"certPassword\":\"%s\",\"userName\":\"%s\",\"identity\":\"%s\",\"passportNo\":\"%s\",\"issueDate\":\"%s\",\"expirationDate\":\"%s\",\"birthDate\":\"%s\",\"certified_date\":\"%s\"}",
                certificationDto.getCertPassword(),
                certificationDto.getUserName(),
                certificationDto.getIdentity(),
                certificationDto.getPassportNo(),
                certificationDto.getIssueDate(),
                certificationDto.getExpirationDate(),
                certificationDto.getBirthDate(),
                certificationDto.getCertifiedDate()
        );

        String fileName = generatePassportPdfFileName(walletId);
        byte[] result;

        try {
            String destination = wallet.getPdfUrls().get(metadataKey);
            result = handlePdfProcessing(destination, file);
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류 발생", e);
        }

        // RSA 암호화 및 복호화 키 설정
        PublicKey publicKey = wallet.getPublicKeyDecoder();  // 공개키 객체로 변환

        String encryptedMetadata;
        // 공개키로 암호화
        try {
            encryptedMetadata = encryptMetadata(metadataString, publicKey);
        } catch (Exception e) {
            throw new RuntimeException("메타데이터 암호화 중 오류 발생", e);
        }

        HashMap<String, String> metadata = new HashMap<>();

        metadata.put(metadataKey, encryptedMetadata);

        log.info("metadata = {}", metadata);
        uploadToS3(fileName, metadata, result);

        // PDF 파일 해시값 생성
        String pdfHash = generatePdfHash(result);
        // 지갑에 해시값 추가
        wallet.updatePdfHash(metadataKey, pdfHash);
        wallet.updatePdfUrl(metadataKey, getPdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);
    }

    private String generatePassportPdfFileName(Long walletId) {
        return walletId + "_passport_certification.pdf";
    }

    private String generatePdfHash(byte[] fileData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileData);
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    public List<Map.Entry<String, String>> getContentsForCertName(String pdfUrl, String certName, Long walletId) {
        try {
            // 파일 이름 추출 및 S3에서 메타데이터 가져오기
            String fileName = extractKeyFromUrl(pdfUrl);
            GetObjectResponse getObjectResponse = s3Client.getObject(getGetObjectRequest(fileName)).response();

            // 주어진 certName과 walletId에 해당하는 메타데이터 찾기
            String value = Optional.ofNullable(
                    decodeMetadata(getObjectResponse.metadata()).entrySet().stream()
                            .filter(entry -> entry.getKey().startsWith(certName + "_") && entry.getKey().endsWith(walletId.toString()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("조건에 맞는 메타데이터가 없습니다."))
            ).orElseThrow();

            log.info("[RSA 디코딩 및 JSON 파싱 진행중]");
            log.info("암호화된 메타데이터: {}", value);

            // 월렛에서 개인키 가져오기
            PrivateKey privateKey = walletRepository.findById(walletId)
                    .map(Wallet::getPrivateKeyDecoder)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            String decryptedValue = decryptMetadata(value, privateKey);
            log.info("복호화된 메타데이터: {}", decryptedValue);
            log.info("[메타데이터 전송 완료]");
            // JSON 파싱 후 (key, value) 형태로 변환하여 리스트로 반환
            return new ObjectMapper().readValue(decryptedValue, new TypeReference<Map<String, Object>>() {})
                    .entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toString()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();  // 예외 발생 시 빈 리스트 반환
        }
    }

    //pdf 병합 로직
    private byte[] handlePdfProcessing(String destination, MultipartFile file) throws IOException {
        byte[] first = (destination != null) ? getBytes(destination) : null;

        if (file.getSize() > 0) {
            byte[] second = getFileBytes(file);
            return (first != null) ? mergePdfs(first, second) : second; // 기존 PDF와 병합하거나 새 PDF 반환
        }

        if (first != null) {
            log.warn("업로드할 파일이 없습니다. 병합을 수행하지 않습니다.");
            return first; // 기존 PDF만 사용
        }

        // PDF가 존재하지 않으면 새로 생성합니다.
        return createEmptyPdf();
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
    public String replacePdfPage(Wallet wallet, String certificateType, int pageNumberToRemove, MultipartFile newPdfFile) throws IOException {
        // 특정 인증서 타입의 PDF URL 가져오기
        String pdfUrl = wallet.getPdfUrl(certificateType);
        if (pdfUrl == null) {
            throw new RuntimeException("PDF URL not found for certificate type: " + certificateType);
        }

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

        // 리소스 닫기
        try {
            originalDocument.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 최종 PDF URL 업데이트
        wallet.updatePdfUrl(certificateType, pdfUrl);

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
    public HashMap<String, String> getCertListByWalletId(Long walletId) {
        // 지갑을 찾습니다. 주어진 walletId를 사용하여 Wallet 객체를 가져옵니다.
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found")); // 지갑이 존재하지 않으면 예외를 발생시킵니다.

        // 지갑의 PDF URL을 가져옵니다. 지갑의 PDF URL을 Map 형태로 가져옵니다.
        Map<String, String> pdfUrls = wallet.getPdfUrls();
        HashMap<String, String> certList = new HashMap<>(); // 인증서 리스트를 저장할 HashMap을 초기화합니다.

        // 각 PDF URL에 대해 인증서 리스트를 가져옵니다.
        for (String pdfUrl : pdfUrls.values()) { // pdfUrls의 값(PDF URL)을 순회합니다.
            // getCertList 메서드를 호출하여 메타데이터를 가져옵니다.
            HashMap<String, String> metadata = getCertList(pdfUrl);

            // 가져온 메타데이터를 certList에 추가합니다.
            certList.putAll(metadata); // 인증서 리스트에 메타데이터를 병합합니다.
        }

        return certList; // 최종 인증서 리스트를 반환합니다.
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
    public HashMap<String, String> getPdfMetadata(String pdfUrl) {
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
    public Set<String> getCertNamesByWalletId(Long walletId) {
        // 주어진 walletId로 Wallet 객체를 가져옴
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // PDF URL 목록 가져오기
        Map<String, String> pdfUrls = wallet.getPdfUrls();

        Set<String> certNames = new HashSet<>();

        // 각 PDF URL에 대해 인증서 이름 가져오기
        for (String pdfUrl : pdfUrls.values()) {
            certNames.addAll(getCertNames(pdfUrl));
        }

        return certNames; // Set 형태로 반환
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

    private GetObjectRequest getGetObjectRequest(String fileName) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .build();
        return getRequest;
    }

    @Transactional
    public void deleteWalletCertificates(Long walletId) {
        // Wallet 객체를 DB에서 가져옵니다.
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Wallet의 pdfUrls 목록을 순회하며 S3에서 하나씩 삭제합니다.
        wallet.getPdfUrls().forEach((certificateType, url) -> {
            String key = extractKeyFromUrl(url);
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(s3Properties.getS3BucketName())
                        .key(key)
                        .build());
            } catch (S3Exception e) {
                throw new RuntimeException("S3에서 파일 삭제 실패: " + e.getMessage());
            }
        });

        // 모든 PDF URL 삭제 후 pdfUrls 맵을 비웁니다.
        wallet.getPdfUrls().clear(); // 더티 체킹으로 인해 변경사항이 자동 반영됩니다.
    }


    @Transactional // 트랜잭션 관리
    public void deletePdf(String urlToDelete, Long walletId, String certificateType) {
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

            // Wallet의 pdfUrls에서 해당 certificateType 항목 제거
            wallet.getPdfUrls().remove(certificateType);

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete photo from S3: " + e.getMessage());
        }
    }



    @Transactional
    public void deletePdfForPage(Wallet wallet, String certificateType, int pageNumberToRemove) throws IOException, S3UploadException {
        String pdfUrl = wallet.getPdfUrl(certificateType); // 인증서 타입을 전달
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

    public int getPdfPageCount(String pdfUrl) {
        byte[] pdfBytes = getOriginalPdfBytes(pdfUrl); // PDF URL로부터 바이트 배열 가져오기

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF document: " + e.getMessage());
        }
    }

/*
    @PreDestroy
    public void cleanup() {
        s3Client.close();
    }
 */

}