package gift.domain.wish;

import java.util.List;
import java.util.stream.Collectors;

public class WishResponse {
    private Long id;
    private Long productId;
    private int amount;

    public WishResponse(Long id, Long productId, int amount) {
        this.id = id;
        this.productId = productId;
        this.amount = amount;
    }

    public static WishResponse fromModel(Wish wish) {
        return new WishResponse(wish.getId(), wish.getProductId(), wish.getAmount());
    }

    public static List<WishResponse> fromModelList(List<Wish> wishes) {
        return wishes.stream().map(WishResponse::fromModel).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
