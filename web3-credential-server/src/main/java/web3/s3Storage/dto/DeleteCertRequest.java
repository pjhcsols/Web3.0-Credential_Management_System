package web3.s3Storage.dto;

import lombok.Data;

@Data
public class DeleteCertRequest {

    private Long walletId;
    private int page;

}