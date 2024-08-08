package gift.service.order;

import gift.domain.order.Order;
import gift.domain.order.OrderRequest;
import gift.domain.product.option.ProductOption;
import gift.domain.user.User;
import gift.repository.order.OrderRepository;
import gift.repository.product.option.ProductOptionRepository;
import gift.repository.wish.WishRepository;
import gift.service.point.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class OrderService {

    @Value("${point.earn.rate}")
    private double pointEarnRate;

    private final OrderRepository orderRepository;
    private final OrderProcessingService orderProcessingService;


    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderProcessingService orderProcessingService) {
        this.orderRepository = orderRepository;
        this.orderProcessingService = orderProcessingService;
    }

    public Page<Order> getAllOrders(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order createOrder(User user, OrderRequest orderRequest) {
        return orderProcessingService.processOrder(user, orderRequest);
    }

    public Page<Order> getOrdersByUser(User user, int page, int size, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return orderRepository.findByUser(user, pageable);
    }
}
