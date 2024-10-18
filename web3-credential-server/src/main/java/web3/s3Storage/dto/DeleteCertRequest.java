package web3.s3Storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class DeleteCertRequest {

    private Long walletId;
    private int page;

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public void setPage(int page) {
        this.page = page;
    }
}