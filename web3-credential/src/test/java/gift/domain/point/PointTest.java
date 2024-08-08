package gift.domain.point;

import gift.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PointTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class); // Mockito를 사용하여 Mock 객체 생성
    }

    @Test
    @DisplayName("포인트 사용 시, 금액이 null일 경우 예외를 발생시켜야 한다")
    void useShouldThrowExceptionWhenAmountIsNull() {
        Point point = new Point(user, 1000L);
        assertThrows(IllegalArgumentException.class, () -> point.use(null));
    }

    @Test
    @DisplayName("포인트 사용 시, 금액이 음수일 경우 예외를 발생시켜야 한다")
    void useShouldThrowExceptionWhenAmountIsNegative() {
        Point point = new Point(user, 1000L);
        assertThrows(IllegalArgumentException.class, () -> point.use(-100L));
    }

    @Test
    @DisplayName("포인트 사용 시, 금액이 차감되어야 한다")
    void useShouldDeductAmount() {
        Point point = new Point(user, 1000L);
        point.use(200L);
        assertEquals(800L, point.getAmount());
    }

    @Test
    @DisplayName("포인트 추가 시, 금액이 null일 경우 예외를 발생시켜야 한다")
    void addShouldThrowExceptionWhenAmountIsNull() {
        Point point = new Point(user, 1000L);
        assertThrows(IllegalArgumentException.class, () -> point.add(null));
    }

    @Test
    @DisplayName("포인트 추가 시, 금액이 음수일 경우 예외를 발생시켜야 한다")
    void addShouldThrowExceptionWhenAmountIsNegative() {
        Point point = new Point(user, 1000L);
        assertThrows(IllegalArgumentException.class, () -> point.add(-100L));
    }

    @Test
    @DisplayName("포인트 추가 시, 금액이 증가해야 한다")
    void addShouldIncreaseAmount() {
        Point point = new Point(user, 1000L);
        point.add(500L);
        assertEquals(1500L, point.getAmount());
    }
}
