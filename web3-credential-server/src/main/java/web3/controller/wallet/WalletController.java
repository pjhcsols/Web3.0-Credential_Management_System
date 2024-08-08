package web3.controller.wallet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web3.domain.user.User;
import web3.domain.wallet.Wallet;
import web3.exception.wallet.WalletAlreadyExistsException;
import web3.service.wallet.WalletService;
import web3.validation.LoginMember;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "로그인된 사용자의 지갑 조회",
            description = "로그인된 사용자의 지갑 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지갑 정보 반환"),
                    @ApiResponse(responseCode = "404", description = "지갑을 찾을 수 없음")
            }
    )
    public ResponseEntity<Wallet> getMyWallet(@LoginMember User loginUser) {
        Optional<Wallet> wallet = walletService.getWalletByUser(loginUser);
        return wallet.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "지갑 생성",
            description = "로그인한 사용자의 지갑을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "지갑 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    public ResponseEntity<Wallet> createWallet(
            @LoginMember User loginUser, // 로그인한 사용자 정보 주입
            @RequestParam String privateKey,
            @RequestParam String publicKey,
            @RequestParam String address) throws WalletAlreadyExistsException {
        Wallet wallet = walletService.createWallet(loginUser, privateKey, publicKey, address);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "지갑 업데이트",
            description = "특정 지갑의 정보를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지갑 업데이트 성공"),
                    @ApiResponse(responseCode = "404", description = "지갑을 찾을 수 없음")
            }
    )
    public ResponseEntity<Wallet> updateWallet(
            @PathVariable Long id,
            @RequestParam String privateKey,
            @RequestParam String publicKey,
            @RequestParam String address) {
        Wallet updatedWallet = walletService.updateWallet(id, privateKey, publicKey, address);
        return ResponseEntity.ok(updatedWallet);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "지갑 삭제",
            description = "특정 지갑을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "지갑 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "지갑을 찾을 수 없음")
            }
    )
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "모든 지갑 조회",
            description = "모든 지갑 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지갑 목록 반환")
            }
    )
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }
}
