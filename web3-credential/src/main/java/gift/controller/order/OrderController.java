package gift.controller.order;

import gift.domain.order.Order;
import gift.domain.order.OrderRequest;
import gift.domain.user.User;
import gift.service.order.OrderService;
import gift.service.user.UserService;
import gift.validation.LoginMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDateTime") String sort,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<Order> orders = orderService.getAllOrders(page, size, sort, sortOrder);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @LoginMember User loginUser,  // 로그인한 사용자 정보를 받음
            @RequestBody OrderRequest orderRequest) {

        Order createdOrder = orderService.createOrder(loginUser, orderRequest);

        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<Order>> getUserOrders(
            @LoginMember User loginUser,  // 로그인한 사용자 정보를 받음
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDateTime") String sort,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<Order> orders = orderService.getOrdersByUser(loginUser, page, size, sort, sortOrder);

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

}




