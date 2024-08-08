package gift.exception.wish;

public class WishCanNotModifyException extends RuntimeException {
    public WishCanNotModifyException() {
        super("Wish cannot be modified.");
    }
}
