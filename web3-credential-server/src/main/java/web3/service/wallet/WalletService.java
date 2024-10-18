package web3.service.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import web3.domain.user.User;
import web3.domain.wallet.Wallet;
import web3.exception.wallet.WalletAlreadyExistsException;
import web3.exception.wallet.WalletPrivateKeyNotEqualsException;
import web3.repository.wallet.WalletRepository;

import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;


    @Autowired
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    //지갑 생성
    public Wallet createWallet(User user, String privateKey, String publicKey) throws WalletAlreadyExistsException {
        Optional<Wallet> existingWallet = walletRepository.findByUser(user);
        if (existingWallet.isPresent()) {
            throw new WalletAlreadyExistsException("User already has a wallet");
        }
        Wallet wallet = new Wallet(user, privateKey, publicKey);
        return walletRepository.save(wallet);
    }

    // 지갑 조회
    public Optional<Wallet> getWalletById(Long id) {
        return walletRepository.findById(id);
    }

    // 사용자와 관련된 지갑 조회
    public Optional<Wallet> getWalletByUser(User user) {
        return walletRepository.findByUser(user);
    }

    // 지갑 업데이트,더티체킹 update 사용안함
    public Wallet updateWallet(Long id, String privateKey, String publicKey, String address) {
        Wallet existingWallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        existingWallet.updateWallet(privateKey, publicKey);
        return walletRepository.save(existingWallet);
    }

    // 지갑 삭제
    public void deleteWallet(Long id) {
        walletRepository.deleteById(id);
    }

    // 모든 지갑 조회
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getCertainWallet(Long id, String privateKey) throws WalletPrivateKeyNotEqualsException{
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

       /* // 사용자 로컬 프라이빗키 <-> wallet 프라이빗 키 대조
        if(!matchPrivateKey(privateKey, wallet)){
            throw new WalletPrivateKeyNotEqualsException("Private key does not match");
        }*/

        return wallet;
    }

    private boolean matchPrivateKey(String privateKey, Wallet wallet) {
        return wallet.getPrivateKey().equals(privateKey);
    }

}
