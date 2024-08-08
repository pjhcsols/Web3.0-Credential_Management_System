package gift.domain.order;

public class OrderRequest {
    private Long optionId;
    private Long quantity;
    private String message;
    private Long pointsToUse;  // 새로운 필드 추가


    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public Long getQuantity() {  // Return type changed to Long
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPointsToUse() {
        return pointsToUse;
    }

    public void setPointsToUse(Long pointsToUse) {
        this.pointsToUse = pointsToUse;
    }
}
