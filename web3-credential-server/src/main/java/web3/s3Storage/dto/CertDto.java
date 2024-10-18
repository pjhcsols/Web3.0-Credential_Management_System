package web3.s3Storage.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@RequiredArgsConstructor
public class CertDto {
    private String certName;
    private HashMap<String, String> certContents;

    // Getters and Setters
}
