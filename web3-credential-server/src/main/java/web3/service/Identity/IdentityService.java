package web3.service.Identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import web3.service.dto.Identity.StudentCertificationDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

@Service
public class IdentityService {
    private static final Logger logger = LoggerFactory.getLogger(IdentityService.class);
    private Gateway gateway;
    private Network network;
/*
    public IdentityService() {
        try {
            Path walletPath = Paths.get("wallet");
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            InputStream networkConfigStream = new ClassPathResource("connection.yaml").getInputStream();

            if (wallet.get("admin") == null) {
                throw new IllegalArgumentException("Identity 'admin' not found in wallet.");
            }

            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(wallet, "admin").networkConfig(networkConfigStream).discovery(true);

            this.gateway = builder.connect();
            this.network = gateway.getNetwork("mychannel");
            logger.info("블록체인 네트워크에 성공적으로 연결되었습니다.");
        } catch (IOException e) {
            logger.error("블록체인 네트워크 연결 실패: {}", e.getMessage());
            throw new RuntimeException("블록체인 네트워크 초기화 실패", e);
        }
    }

 */


    // 재학증 등록
    public void registerStudentCertification(StudentCertificationDto certificationDto) {
        // 블록체인에 저장할 StudentCertification 데이터 형식 생성
        String certificationData = String.format("email:%s/univName:%s/univ_check:%b",
                certificationDto.getEmail(),
                certificationDto.getUnivName(),
                certificationDto.isUnivCheck());

        // 블록체인에 데이터 저장
        try {
            putDataOnBlockchain(certificationDto.getKey(), certificationData);
        } catch (ContractException | TimeoutException | InterruptedException e) {
            logger.error("체인코드 트랜잭션 제출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("블록체인에 데이터 등록 실패", e); // 언체크 예외
        }
    }

    // 재학증 조회
    public StudentCertificationDto queryStudentCertification(String key) {
        // 블록체인에서 데이터 조회
        String data;
        try {
            data = getDataFromBlockchain(key);
        } catch (ContractException | TimeoutException e) {
            logger.error("체인코드 데이터 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("블록체인 데이터 조회 실패", e); // 언체크 예외
        }

        if (data == null) {
            throw new IllegalArgumentException("재학증이 발견되지 않았습니다."); // 언체크 예외
        }

        // 데이터 파싱
        String[] parts = data.split("/");
        String email = parts[0].split(":")[1];
        String univName = parts[1].split(":")[1];
        boolean univCheck = Boolean.parseBoolean(parts[2].split(":")[1]);

        return new StudentCertificationDto(key, email, univName, univCheck);
    }

    private void putDataOnBlockchain(String key, String data) throws ContractException, TimeoutException, InterruptedException {
        submitTransaction("putData", key, data);
    }

    private String getDataFromBlockchain(String key) throws ContractException, TimeoutException {
        return evaluateTransaction("getData", key);
    }

    private void submitTransaction(String functionName, String... args) throws ContractException, TimeoutException, InterruptedException {
        byte[] result = this.network.getContract("certification").submitTransaction(functionName, args);
        logger.info("트랜잭션이 제출되었습니다: " + new String(result));
    }

    private String evaluateTransaction(String functionName, String... args) throws ContractException, TimeoutException {
        byte[] result = this.network.getContract("certification").evaluateTransaction(functionName, args);
        return new String(result);
    }
}
