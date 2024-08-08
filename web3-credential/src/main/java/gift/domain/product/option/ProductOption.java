package gift.domain.product.option;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gift.domain.product.Product;
import jakarta.persistence.*;

import java.util.regex.Pattern;

@Entity
@Table(name = "product_option", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "name"})
})
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Long quantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    protected ProductOption() {}

    public ProductOption(String name, Long quantity, Product product) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("잘못된 옵션 이름입니다");
        }
        if (quantity < 1 || quantity >= 100000000) {
            throw new IllegalArgumentException("수량은 1에서 100,000,000 사이여야 합니다");
        }
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    private boolean isValidName(String name) {
        String regex = "^[a-zA-Z0-9가-힣 ()\\[\\]+\\-&/_]+$";

        return Pattern.matches(regex, name);
    }


    public void subtract(Long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감할 수량은 양수여야 합니다");
        }
        if (this.quantity < quantity) {
            throw new IllegalArgumentException("수량이 부족합니다");
        }
        this.quantity -= quantity;
    }

    public ProductOption withUpdatedNameAndQuantity(String name, Long quantity) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid option name");
        }
        if (quantity < 1 || quantity >= 100000000) {
            throw new IllegalArgumentException("Quantity must be between 1 and 100,000,000");
        }
        this.name = name;
        this.quantity = quantity;
        return this;
    }

    public Long getPrice() {
        return product.getPrice(); // Product 객체에서 가격을 가져옴
    }
}
