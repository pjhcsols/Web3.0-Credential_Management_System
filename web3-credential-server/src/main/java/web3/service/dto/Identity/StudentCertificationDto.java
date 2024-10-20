package web3.service.dto.Identity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudentCertificationDto {
    private String email;
    private String univName;
    private boolean univCheck;
    private LocalDateTime certifiedDate; // 새로운 필드 추가


    public StudentCertificationDto(String email, String univName, boolean univCheck, LocalDateTime certifiedDate) {
        this.email = email;
        this.univName = univName;
        this.univCheck = univCheck;
        this.certifiedDate = certifiedDate; // 새로운 필드 초기화
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

    public String getCertifiedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return certifiedDate.format(formatter);
    }
}
