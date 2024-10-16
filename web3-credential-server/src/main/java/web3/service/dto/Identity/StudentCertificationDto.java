package web3.service.dto.Identity;

public class StudentCertificationDto {
    private String key; //재학증_walletId
    private String email;
    private String univName;
    private boolean univCheck;

    public StudentCertificationDto(String key, String email, String univName, boolean univCheck) {
        this.key = key;
        this.email = email;
        this.univName = univName;
        this.univCheck = univCheck;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUnivName() {
        return univName;
    }

    public void setUnivName(String univName) {
        this.univName = univName;
    }

    public boolean isUnivCheck() {
        return univCheck;
    }

    public void setUnivCheck(boolean univCheck) {
        this.univCheck = univCheck;
    }
}
