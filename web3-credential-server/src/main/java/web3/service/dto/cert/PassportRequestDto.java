package web3.service.dto.cert;

public class PassportRequestDto {
    private String certFileEncoded;         // 인증서 파일 Base64 인코딩
    private String keyFileEncoded;          // 키 파일 Base64 인코딩
    private String certPassword;     // 인증서 비밀번호
    private String userName;         // 사용자 이름
    private String identity;         // 주민등록번호
    private String passportNo;       // 여권 번호
    private String issueDate;        // 발급일
    private String expirationDate;   // 만료일
    private String birthDate;        // 생년월일

    public PassportRequestDto(String certFileEncoded, String keyFileEncoded, String certPassword,
                              String userName, String identity, String passportNo,
                              String issueDate, String expirationDate, String birthDate) {
        this.certFileEncoded = certFileEncoded;
        this.keyFileEncoded = keyFileEncoded;
        this.certPassword = certPassword;
        this.userName = userName;
        this.identity = identity;
        this.passportNo = passportNo;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.birthDate = birthDate;
    }

    // Getters
    public String getCertFileEncoded() {
        return certFileEncoded;
    }

    public String getKeyFileEncoded() {
        return keyFileEncoded;
    }

    public String getCertPassword() {
        return certPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getIdentity() {
        return identity;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
