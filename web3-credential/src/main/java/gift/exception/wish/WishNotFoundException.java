package gift.exception.wish;

public class WishNotFoundException extends RuntimeException {
    public WishNotFoundException(Long wishId) {
        super("Wish not found with ID: " + wishId);
    }
}
