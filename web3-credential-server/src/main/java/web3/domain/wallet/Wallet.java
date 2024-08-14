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

    @Column(nullable = false)
    private String pdfUrl;

    @Column(nullable = false)
    private String privateKey;

    @Column(nullable = false)
    private String publicKey;

    @Column(nullable = false)
    private String address;

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    //JPA 기본 생성자
    protected Wallet() {}

    // 생성자: 필드 값을 모두 제공
    public Wallet(User user, String privateKey, String publicKey, String address) {
        this.user = user;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    public void updateWallet(String privateKey, String publicKey, String address) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    // Getter 메서드
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

    public String getAddress() {
        return address;
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
                ", address='" + address + '\'' +
                '}';
    }
}
