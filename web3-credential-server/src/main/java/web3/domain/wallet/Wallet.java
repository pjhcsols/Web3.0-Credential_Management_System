package web3.domain.wallet;

import jakarta.persistence.*;
import web3.domain.user.User;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String pdfUrl;

    @Column(nullable = false)
    private String privateKey;

    @Column(nullable = false)
    private String publicKey;


    //JPA 기본 생성자
    protected Wallet() {}

    // 생성자: 필드 값을 모두 제공
    public Wallet(User user, String privateKey, String publicKey) {
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void updateWallet(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void updatePdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void updatePrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    // Getter 메서드
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getPdfUrl() {return pdfUrl; }

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
                '}';
    }
}
