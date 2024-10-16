package web3.controller.Identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web3.service.Identity.IdentityService;
import web3.service.dto.Identity.StudentCertificationDto;

@RestController
@RequestMapping("/api/certifications")
public class IdentityController {

    private final IdentityService identityService;

    @Autowired
    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCertification(@RequestBody StudentCertificationDto certificationDto) {
        try {
            identityService.registerStudentCertification(certificationDto);
            return ResponseEntity.ok("재학증이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("재학증 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/{key}")
    public ResponseEntity<StudentCertificationDto> getCertification(@PathVariable String key) {
        try {
            StudentCertificationDto certificationDto = identityService.queryStudentCertification(key);
            return ResponseEntity.ok(certificationDto);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}

