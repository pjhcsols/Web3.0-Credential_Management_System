package web3.service.wallet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web3.domain.user.User;
import web3.domain.wallet.Wallet;
import web3.exception.wallet.WalletAlreadyExistsException;
import web3.exception.wallet.WalletPrivateKeyNotEqualsException;
import web3.properties.SignProperties;
import web3.repository.wallet.WalletRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final SignProperties signProperties;


    @Autowired
    public WalletService(WalletRepository walletRepository, SignProperties signProperties) {
        this.walletRepository = walletRepository;
        this.signProperties = signProperties;
    }

    public Wallet createWallet(User user) throws WalletAlreadyExistsException, NoSuchAlgorithmException {
        Optional<Wallet> existingWallet = walletRepository.findByUser(user);
        if (existingWallet.isPresent()) {
            throw new WalletAlreadyExistsException("User already has a wallet");
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 공개키와 개인키를 PEM 형식의 문자열로 변환
        String publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyString = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        // 지갑 생성
        Wallet wallet = new Wallet(user, privateKeyString, publicKeyString);
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
    public Wallet updateWallet(Long id, String privateKey, String publicKey) {
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

    @Transactional
    public void storeCertificateFiles(MultipartFile signCert, MultipartFile signPri, Wallet wallet) throws IOException {
        log.info("[인증서 저장 로직 시작]");
        try {
            // 기존 파일 경로가 있으면 삭제 (signCertPath, signPriKeyPath)
            if (wallet.getSignCertPath() != null) {
                Path oldCertPath = Paths.get(wallet.getSignCertPath());
                Files.deleteIfExists(oldCertPath);  // 기존 인증서 파일 삭제
                log.info("기존 인증서 파일 삭제: " + oldCertPath);
            }

            if (wallet.getSignPriKeyPath() != null) {
                Path oldPriPath = Paths.get(wallet.getSignPriKeyPath());
                Files.deleteIfExists(oldPriPath);  // 기존 개인키 파일 삭제
                log.info("기존 개인키 파일 삭제: " + oldPriPath);
            }

            String certFileName = wallet.getId() + "_signCert.der";
            Path certPath = Paths.get(signProperties.getFullSignCertDir(), certFileName);
            Files.write(certPath, signCert.getBytes());
            log.info("새로운 인증서 파일 저장: " + certPath);

            String priFileName = wallet.getId() + "_signPri.key";
            Path priPath = Paths.get(signProperties.getFullSignPriDir(), priFileName);
            Files.write(priPath, signPri.getBytes());
            log.info("새로운 개인키 파일 저장: " + priPath);

            // 파일 경로 업데이트
            wallet.updateSignPaths(certPath.toString(), priPath.toString());
            log.info("지갑 경로 업데이트 완료: certPath = " + certPath + ", priPath = " + priPath);

        } catch (IOException e) {
            log.error("인증서 파일 저장 중 오류 발생", e);
            throw e;
        }
    }




    public String retrieveSignCertEncoded(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        log.info("[지갑의 등록된 SignCert encoding....]");
        return encodeFileToBase64(wallet.getSignCertPath());
    }

    public String retrieveSignPriKeyEncoded(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        log.info("[지갑의 등록된 SignPri encoding....]");
        return encodeFileToBase64(wallet.getSignPriKeyPath());
    }

    private String encodeFileToBase64(String filePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new RuntimeException("파일 인코딩 실패", e);
        }
    }


}
