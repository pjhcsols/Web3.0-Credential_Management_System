package gift.exception.user;

import org.springframework.http.HttpStatus;

public class UserException extends RuntimeException {
    private HttpStatus status;

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
