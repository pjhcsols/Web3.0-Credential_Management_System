package web3.domain.wallet;

import jakarta.persistence.*;
import web3.domain.user.User;
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

    // RSA 암호화를 위한 키
    //uuid를 넣어서 RSA 암호화 할때 같이 사용, 메타데이터 업로드 및 가져올때 디코딩
    @Column(name = "private_key", nullable = false)
    private String privateKey;

    @Column(name = "public_key", nullable = false)
    private String publicKey;

    //공동 인증서 정보 추가?

    protected Wallet() {}

    public Wallet(User user, String privateKey, String publicKey) {
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void updateWallet(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    //key(재학증_1):value(pdfUrl) 로 디비에 저장
    public void updatePdfUrl(String certificateType, String pdfUrl) {
        this.pdfUrls.put(certificateType, pdfUrl);
    }

    // 인증서 타입에 따라 PDF URL 가져오기
    public String getPdfUrl(String certificateType) {
        return this.pdfUrls.get(certificateType);
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
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
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
                '}';
    }

}