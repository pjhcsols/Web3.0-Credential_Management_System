package web3.service.dto.Identity;

public class StudentCertificationDto {
    private String email;
    private String univName;
    private boolean univCheck;

    public StudentCertificationDto(String email, String univName, boolean univCheck) {

        this.email = email;
        this.univName = univName;
        this.univCheck = univCheck;
    }

    public String getEmail() {
        return email;
    }

    public String getUnivName() {
        return univName;
    }

    public boolean isUnivCheck() {
        return univCheck;
    }

}
