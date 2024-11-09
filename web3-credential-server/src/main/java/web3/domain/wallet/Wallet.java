package web3.domain.wallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import web3.domain.user.User;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 인증서별 PDF URL 관리
    @ElementCollection
    @CollectionTable(name = "wallet_pdf_urls", joinColumns = @JoinColumn(name = "wallet_id"))
    @MapKeyColumn(name = "certificate_type")
    @Column(name = "pdf_url")
    private Map<String, String> pdfUrls = new HashMap<>();

    //@JsonIgnore
    @Column(name = "private_key", nullable = false)
    private String privateKey; // value 복호화

    @Column(name = "public_key", nullable = false)
    private String publicKey; // value 암호화

    @ElementCollection
    @CollectionTable(name = "wallet_pdf_hash", joinColumns = @JoinColumn(name = "wallet_id"))
    @MapKeyColumn(name = "certificate_type")
    @Column(name = "pdf_hash")
    private Map<String, String> pdfHash = new HashMap<>(); //PDF 해시 비교로직 구성

    //인증서 signCert.der,signpri.key 파일 을 로컬에 저장하고 주소를 저장해야된다.


    // 기본 생성자
    protected Wallet() {}

    // 생성자
    public Wallet(User user, String privateKey, String publicKey) {
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public Map<String, String> getPdfUrls() {
        return pdfUrls;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getPrivateKey() {
        return privateKey; // String 반환
    }

    public String getPublicKey() {
        return publicKey; // String 반환
    }

    public Map<String, String> getPdfHash() {
        return pdfHash;
    }

    @JsonIgnore // 이 메서드는 JSON 직렬화에서 제외합니다.
    public PublicKey getPublicKeyDecoder() {
        return convertKey(publicKey, true);
    }

    @JsonIgnore // 이 메서드는 JSON 직렬화에서 제외합니다.
    public PrivateKey getPrivateKeyDecoder() {
        return convertKey(privateKey, false);
    }

    private <T> T convertKey(String key, boolean isPublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (isPublicKey) {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                return (T) keyFactory.generatePublic(spec);
            } else {
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                return (T) keyFactory.generatePrivate(spec);
            }
        } catch (Exception e) {
            String keyType = isPublicKey ? "공개키" : "개인키";
            throw new RuntimeException(keyType + " 변환 중 오류 발생", e);
        }
    }

    public void updateWallet(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void addToPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void updatePdfHash(String certificateType, String pdfHash) {
        this.pdfHash.put(certificateType, pdfHash);
    }

    // key(재학증_1):value(pdfUrl) 로 디비에 저장
    public void updatePdfUrl(String certificateType, String pdfUrl) {
        this.pdfUrls.put(certificateType, pdfUrl);
    }

    // 인증서 타입에 따라 PDF URL 가져오기
    public String getPdfUrl(String certificateType) {
        return this.pdfUrls.get(certificateType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return id != null ? id.equals(wallet.id) : wallet.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", user=" + user +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", pdfUrls=" + pdfUrls +
                ", pdfHash=" + pdfHash +
                '}';
    }

}
