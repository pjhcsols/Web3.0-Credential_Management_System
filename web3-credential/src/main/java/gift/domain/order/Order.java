package gift.domain.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import gift.domain.product.option.ProductOption;
import gift.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    private Long quantity;
    private String message;

    @Column(name = "order_date_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDateTime;

    //실 결제 금액
    @Column(name = "remaining_cash_amount", nullable = false)
    private Long remainingCashAmount;
    //사용 포인트
    @Column(name = "points_to_use", nullable = false)
    private Long pointsToUse;


    // Default constructor
    protected Order() {}

    public Order(User user, ProductOption productOption, Long quantity, String message, LocalDateTime orderDateTime, Long remainingCashAmount, Long pointsToUse) {
        this.user = user;
        this.productOption = productOption;
        this.quantity = quantity;
        this.message = message;
        this.orderDateTime = orderDateTime;
        this.remainingCashAmount = remainingCashAmount;
        this.pointsToUse = pointsToUse;
    }

    // Getters only
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ProductOption getProductOption() {
        return productOption;
    }

    public Long getQuantity() {
        return quantity;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public Long getRemainingCashAmount() {
        return remainingCashAmount;
    }

    public Long getPointsToUse() {
        return pointsToUse;
    }
}
