package web3.service.dto.Identity;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Getter
public class PassportCertificationDto {
    private String certFile;
    private String keyFile;
    private String certPassword;
    private String userName;
    private String identity;
    private String passportNo;
    private String issueDate;
    private String expirationDate;
    private String birthDate;
    LocalDateTime certifiedDate;

    // Constructor
    public PassportCertificationDto(String certFile, String keyFile, String certPassword, String userName,
                                    String identity, String passportNo, String issueDate,
                                    String expirationDate, String birthDate, LocalDateTime certifiedDate) {
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.certPassword = certPassword;
        this.userName = userName;
        this.identity = identity;
        this.passportNo = passportNo;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.birthDate = birthDate;
        this.certifiedDate = certifiedDate;
    }

    // Method to decode the Base64 encoded files
    public byte[] decodeCertFile() {
        return Base64.getDecoder().decode(certFile);
    }

    public byte[] decodeKeyFile() {
        return Base64.getDecoder().decode(keyFile);
    }

    // Method to encode the file to Base64
    public static String encodeFileToBase64(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new RuntimeException("파일 인코딩 실패", e);
        }
    }

    public String getCertifiedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return certifiedDate.format(formatter);
    }
}
