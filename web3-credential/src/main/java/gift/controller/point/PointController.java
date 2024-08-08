package gift.controller.point;


import gift.domain.user.User;
import gift.service.point.PointService;
import gift.validation.LoginMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @Autowired
    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping
    public ResponseEntity<?> getUserPoints(@LoginMember User user) {
        return ResponseEntity.ok(pointService.getUserPoints(user));
    }

    @PostMapping
    public ResponseEntity<?> chargePoints(
            @LoginMember User user,
            @RequestParam("amount") Long amount) {
        if (amount < 10000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("최소 충전 금액은 10,000원입니다.");
        }
        pointService.addPoints(user, amount);
        return ResponseEntity.ok("포인트 충전 완료");
    }
}
