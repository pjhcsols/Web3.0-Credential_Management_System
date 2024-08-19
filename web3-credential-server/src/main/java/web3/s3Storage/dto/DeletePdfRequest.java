package web3.s3Storage.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class DeletePdfRequest {
    private String urlToDelete;

}
