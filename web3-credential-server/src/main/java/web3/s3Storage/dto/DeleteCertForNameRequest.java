package web3.s3Storage.dto;

import lombok.Data;

@Data
public class DeleteCertForNameRequest {

    private Long walletId;
    private String certName;

}