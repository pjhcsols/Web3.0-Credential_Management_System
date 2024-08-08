package gift.exception.user;

import org.springframework.http.HttpStatus;

public class UserAuthorizedErrorException extends UserException {
    private final static String MESSAGE = "로그인 정보가 잘못되었습니다. 다시 로그인해주세요";
    private final static HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;

    public UserAuthorizedErrorException() {
        super(MESSAGE, HTTP_STATUS);
    }
}
