package gift.service.order;

import gift.domain.order.Order;
import gift.domain.order.OrderRequest;
import gift.domain.product.option.ProductOption;
import gift.domain.user.User;
import gift.repository.order.OrderRepository;
import gift.repository.product.option.ProductOptionRepository;
import gift.repository.wish.WishRepository;
import gift.service.point.PointService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderProcessingService {

    @Value("${point.earn.rate}")
    private double pointEarnRate;

    private final ProductOptionRepository productOptionRepository;
    private final PointService pointService;
    private final WishRepository wishRepository;
    private final OrderRepository orderRepository;

    public OrderProcessingService(ProductOptionRepository productOptionRepository,
                                  PointService pointService,
                                  WishRepository wishRepository,
                                  OrderRepository orderRepository) {
        this.productOptionRepository = productOptionRepository;
        this.pointService = pointService;
        this.wishRepository = wishRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order processOrder(User user, OrderRequest orderRequest) {
        ProductOption productOption = productOptionRepository.findById(orderRequest.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없음"));

        Long totalPrice = productOption.getPrice() * orderRequest.getQuantity();
        Long pointsToUse = Math.min(orderRequest.getPointsToUse(), totalPrice);
        pointsToUse = Math.min(pointsToUse, pointService.getUserPoints(user));

        // 포인트 사용 및 적립
        pointService.usePoints(user, pointsToUse);
        Long remainingCashAmount = totalPrice - pointsToUse;
        Long pointsToEarn = (long) (remainingCashAmount * pointEarnRate);
        pointService.addPoints(user, pointsToEarn);

        // 제품 옵션 수량 감소
        productOption.subtract(orderRequest.getQuantity());
        productOptionRepository.save(productOption);

        // 위시리스트 삭제
        wishRepository.findByUserIdAndProductIdAndIsDeletedFalse(user.getId(), productOption.getProduct().getId())
                .ifPresent(wish -> {
                    wish.setIsDeleted(true);
                    wishRepository.save(wish);
                });

        // 주문 생성
        Order order = new Order(user, productOption, orderRequest.getQuantity(), orderRequest.getMessage(), LocalDateTime.now(), remainingCashAmount, pointsToUse);
        return orderRepository.save(order);
    }
}
