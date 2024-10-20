package web3.s3Storage.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.ParameterMap;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    @Transactional
    public String uploadPdf(MultipartFile file, Wallet wallet,String certName,Map<String,String> certContents) throws IOException {
        String fileName;
        byte[] result;
        HashMap<String, String> metadata = new HashMap<>();
        int page;
        // 첫 등록일 때 => 생성해줘야 함
        if (wallet.getPdfUrl() == null) {
            fileName = (file.getSize() > 0) ? getFileName(wallet) : getEmptyFilename(wallet);
            result = (file.getSize() > 0) ? getFileBytes(file) : createEmptyPdf();
            page = getPdfPageCount(result);
        } else {
            // 이미 있을 시 -> PDF 병합
            String destination = wallet.getPdfUrl();
            fileName = extractKeyFromUrl(destination);

            byte[] first; // 원래 파일
            first = getBytes(destination);
            byte[] second = (file.getSize() > 0) ? getFileBytes(file) : createEmptyPdf(); // 뒤에 들어온 파일

            result = mergePdfs(first, second);

            metadata = decodeMetadata(getPdfMetadata(fileName));
            page = getPdfPageCount(second);
        }

        // contents 해시맵의 키-값 쌍을 ':'로 구분하여 value에 추가
        StringBuilder valueBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : certContents.entrySet()) {
            valueBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("/");
        }

        for(int now = 0; now < page; now ++){
            String key = certName + "_" + wallet.getId() + "_" + now; // 키 설정
            String value = valueBuilder.toString();
            metadata.put(key, value); // 키-값 쌍으로 추가
        }

        log.info("metadata = {}", metadata);

        uploadToS3(fileName, metadata, result);

        wallet.updatePdfUrl(getPdfUrl(fileName));
        walletRepository.saveAndFlush(wallet);

        return getPdfUrl(fileName);
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

    private static String getFileName(Wallet wallet) {
        return wallet.getId() + "_" + System.currentTimeMillis() + "_" + wallet.getId();
    }

    private static String getEmptyFilename(Wallet wallet) {
        return wallet.getId() + "_" + System.currentTimeMillis() + "_" + "empty.pdf";
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
    public String replacePdfPage(Wallet wallet, String certName, MultipartFile newPdfFile) throws IOException{
        // 원래 PDF 가져오기
        String pdfUrl = wallet.getPdfUrl();
        byte[] originalPdfBytes = getOriginalPdfBytes(pdfUrl);
        String fileName = extractKeyFromUrl(pdfUrl);

        // 기존 PDF 로드와 리소스 관리
        PDDocument originalDocument = loadOriginalDocument(originalPdfBytes);

        int totalPages = originalDocument.getNumberOfPages();
        int pdfPageCount = getPdfPageCount(newPdfFile.getBytes());

        LinkedHashMap<String, String> metadata = decodeMetadata(getPdfMetadata(wallet.getPdfUrl()));

        int firstPageIndexToRemove = -1;
        int lastPageIndexToRemove = -1;

        for (int i = 0; i < metadata.size(); i++) {
            String key = (String) metadata.keySet().toArray()[i];
            String[] parts = key.split("_");

            if (parts.length > 0 && parts[0].equals(certName)) {
                if (firstPageIndexToRemove == -1) {
                    firstPageIndexToRemove = i;
                }
                lastPageIndexToRemove = i;
            }
        }

        // 페이지 번호는 0부터 시작 하므로 1을 빼줌

        // 페이지가 존재하는지 확인
        if (firstPageIndexToRemove < 0 || lastPageIndexToRemove >= totalPages) {
            throw new IllegalArgumentException("Page number out of range");
        }

        // 새로운 PDF 파일 로드
        byte[] newPdfBytes = getFileBytes(newPdfFile);

        // 앞 부분과 뒷부분 PDF 바이트 배열 생성
        byte[] frontPart = createPdfBytesPart(originalDocument, 0, firstPageIndexToRemove);
        byte[] backPart = createPdfBytesPart(originalDocument, lastPageIndexToRemove + 1, originalDocument.getNumberOfPages());

        // PDF 합치기
        byte[] finalPdfBytes = mergeThreePdfs(frontPart, newPdfBytes, backPart);
        LinkedHashMap<String, String> newMetadata = decodeMetadata(getPdfMetadata(fileName));

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("_"); // '_'로 분리

            // 메타데이터 추가
            newMetadata.put(key, entry.getValue());

            // 앞부분이 certName과 같을 경우
            if (parts.length > 0 && parts[0].equals(certName)) {
                // 새로운 키를 추가
                for (int i = 1; i < pdfPageCount; i++) {
                    String newKey = certName + "_" + wallet.getId() + "_" + i;
                    newMetadata.put(newKey, entry.getValue()); // 같은 값을 사용하여 추가
                }

            }
        }

        // 최종 PDF를 S3에 업로드
        uploadToS3(fileName, newMetadata, finalPdfBytes);

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

            if (key != null && key.contains("_")) {
                String lastTwoChars = key.substring(key.length() - 2);
                System.out.println("lastTwoChars = " + lastTwoChars);
                if (lastTwoChars.equals("_0")){
                    certList.put(key, value);
                }
            }
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

   /* @Transactional
    public String getMetadataForPage(String pdfUrl, int pageNumber) {
        String fileName = extractKeyFromUrl(pdfUrl);

        GetObjectRequest getRequest = getGetObjectRequest(fileName);

        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();

        Map<String, String> metadata = getObjectResponse.metadata();
        HashMap<String, String> decodedMetadata = decodeMetadata(metadata);

        String pageKey = "page-" + pageNumber;

        String result = decodedMetadata.get(pageKey);

        // 내용 부분만 추출
        if (result != null && result.contains(":")) {
            return result.split(":")[0]; // ':'기준으로 분리
        }

        return null; // 결과가 없거나 ':'가 없는 경우 null 반환
    }
*/
    public List<Map.Entry<String, String>> getContentsForCertName(String pdfUrl, String certName) {
        String fileName = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getRequest = getGetObjectRequest(fileName);
        GetObjectResponse getObjectResponse = s3Client.getObject(getRequest).response();
        String value = null;
        List<Map.Entry<String, String>> tuples = new ArrayList<>();
        Map<String, String> metadata = decodeMetadata(getObjectResponse.metadata());
        for (String key : metadata.keySet()) {

            if (key.startsWith(certName + "_") && key.endsWith("_0")){
                value = metadata.get(key);

                break;
            }
        }

        // '/'로 분리
        String[] pairs = Objects.requireNonNull(value).split("/");

        // 각 쌍을 (key, value) 형태로 변환하여 리스트에 추가
        for (String pair : pairs) {
            if (!pair.isEmpty()) { // 빈 문자열 체크
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    tuples.add(new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]));
                }
            }
        }
        System.out.println("tuples = " + tuples);
        return tuples;
    }

    private GetObjectRequest getGetObjectRequest(String fileName) {
        return GetObjectRequest.builder()
                .bucket(s3Properties.getS3BucketName())
                .key(fileName)
                .build();
    }

    public int getPdfPageCount(byte[] pdfBytes){
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validatePdfFile(String filename) {
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Illegal end name. Only Pdf.");
        }
    }

    public void deletePdf(String urlToDelete) {
        String key = extractKeyFromUrl(urlToDelete);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getS3BucketName())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete photo from S3: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePdfForCertName(Wallet wallet, String certName) throws IOException, S3UploadException {
        String pdfUrl = wallet.getPdfUrl();
        byte[] originalPdfBytes = getPdf(pdfUrl).readAllBytes();
        String fileName = extractKeyFromUrl(pdfUrl);

        // 기존 PDF 로드와 리소스 관리
        try (PDDocument originalDocument = PDDocument.load(new ByteArrayInputStream(originalPdfBytes))) {
            int totalPages = originalDocument.getNumberOfPages();
            LinkedHashMap<String, String> metadata = decodeMetadata(getPdfMetadata(wallet.getPdfUrl()));

            int firstPageIndexToRemove = -1;
            int lastPageIndexToRemove = -1;

            for (int i = 0; i < metadata.size(); i++) {
                String key = (String) metadata.keySet().toArray()[i];
                String[] parts = key.split("_");

                if (parts.length > 0 && parts[0].equals(certName)) {
                    if (firstPageIndexToRemove == -1) {
                        firstPageIndexToRemove = i;
                    }
                    lastPageIndexToRemove = i;
                }
            }
            System.out.println("firstPageIndexToRemove = " + firstPageIndexToRemove);
            System.out.println("lastPageIndexToRemove = " + lastPageIndexToRemove);

            // 페이지가 존재하는지 확인
            checkPageExist(firstPageIndexToRemove < 0, lastPageIndexToRemove >= totalPages, "Page number out of range: " + lastPageIndexToRemove);

            byte[] frontPart = createPdfBytesPart(originalDocument, 0, firstPageIndexToRemove);
            byte[] backPart = createPdfBytesPart(originalDocument, lastPageIndexToRemove + 1, originalDocument.getNumberOfPages());

            // PDF 합치기
            byte[] finalPdfBytes = mergePdfs(frontPart, backPart);

            // 기존 키들을 리스트로 변환 후 정렬
            HashMap<String, String> newMetadata = deleteMetadataForName(certName, metadata);

            // 최종 PDF를 S3에 업로드
            try {
                uploadToS3(fileName, newMetadata, finalPdfBytes);
            } catch (S3Exception e) {
                throw new S3UploadException("Failed to upload pdf to S3: " + e.getMessage());
            }
        }
    }

    public LinkedHashMap<String, String> deleteMetadataForName(String certName, LinkedHashMap<String, String> metadata) {
        LinkedHashMap<String, String> updatedMetadata = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("_"); // '_'로 분리

            // 앞부분이 certName과 일치하지 않으면 그대로 추가
            if (parts.length == 0 || !parts[0].equals(certName)) {
                updatedMetadata.put(key, entry.getValue());
            }
        }

        return updatedMetadata; // 업데이트된 메타데이터 반환
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

    @Transactional
    public byte[] getPdfByCertName(String pdfUrl, String certName) throws IOException {
        String key = extractKeyFromUrl(pdfUrl);
        GetObjectRequest getObjectRequest = getGetObjectRequest(key); // getGetObjectRequest 메서드 사용
        HashMap<String, String> metadata = decodeMetadata(getPdfMetadata(pdfUrl));

        int targetPageIndex = -1;
        int currentIndex = 0;

        for (String k : metadata.keySet()) {

            String[] parts = k.split("_");
            if (parts[0].equals(certName)) {
                targetPageIndex = currentIndex;
                break;
            }
            currentIndex++;
        }

        return getPageFromPdf(key, targetPageIndex);

    }

    public byte[] getPageFromPdf(String key, int pageNumber) throws IOException {
        // S3에서 PDF 파일 가져오기
        GetObjectRequest request = getGetObjectRequest(key);
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(request);

        try (PDDocument document = PDDocument.load(s3Object)) {
            PDPageTree pages = document.getPages();


            // 특정 페이지 추출
            PDDocument singlePageDocument = new PDDocument();
            singlePageDocument.addPage(pages.get(pageNumber - 1)); // 페이지 번호는 0부터 시작하므로 -1

            // ByteArrayOutputStream에 저장
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            singlePageDocument.save(outputStream);
            singlePageDocument.close();

            // byte[] 형태로 반환
            return outputStream.toByteArray();
        } catch (IOException e) {
            // 예외 처리: 필요에 따라 로깅이나 추가적인 처리
            throw new IOException("PDF 문서를 처리하는 중 오류가 발생했습니다.", e);
        }
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

}