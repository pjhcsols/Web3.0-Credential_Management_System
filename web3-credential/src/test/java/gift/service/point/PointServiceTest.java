package gift.service.point;

import gift.domain.point.Point;
import gift.domain.user.User;
import gift.repository.point.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = mock(User.class); // Mockito를 사용하여 Mock 객체 생성
    }

    @Test
    @DisplayName("포인트가 존재할 경우, 유저의 포인트를 반환해야 한다")
    void getUserPointsShouldReturnPoints() {
        Point point = new Point(user, 1000L);
        when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

        Long points = pointService.getUserPoints(user);

        assertEquals(1000L, points);
        verify(pointRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("포인트가 존재하지 않을 경우, 0을 반환해야 한다")
    void getUserPointsShouldReturnZeroIfNoPointsFound() {
        when(pointRepository.findByUser(user)).thenReturn(Optional.empty());

        Long points = pointService.getUserPoints(user);

        assertEquals(0L, points);
        verify(pointRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("포인트가 부족할 경우, 예외를 발생시켜야 한다")
    void usePointsShouldThrowExceptionWhenInsufficientPoints() {
        Point point = new Point(user, 500L);
        when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

        assertThrows(IllegalArgumentException.class, () -> pointService.usePoints(user, 1000L));
        verify(pointRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("포인트 사용 시, 포인트가 차감되어야 한다")
    void usePointsShouldDeductPoints() {
        Point point = new Point(user, 1000L);
        when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

        pointService.usePoints(user, 500L);

        assertEquals(500L, point.getAmount());
        verify(pointRepository, times(1)).findByUser(user);
        verify(pointRepository, times(1)).save(point);
    }

    @Test
    @DisplayName("포인트 적립 시, 포인트가 증가해야 한다")
    void addPointsShouldIncreaseAmount() {
        Point point = new Point(user, 1000L);
        when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

        pointService.addPoints(user, 500L);

        assertEquals(1500L, point.getAmount());
        verify(pointRepository, times(1)).findByUser(user);
        verify(pointRepository, times(1)).save(point);
    }

    @Test
    @DisplayName("포인트가 존재하지 않을 경우, 새로운 포인트를 생성해야 한다")
    void addPointsShouldCreatePointIfNotExist() {
        when(pointRepository.findByUser(user)).thenReturn(Optional.empty());

        pointService.addPoints(user, 500L);

        verify(pointRepository, times(1)).findByUser(user);
        verify(pointRepository, times(1)).save(any(Point.class));
    }
}
