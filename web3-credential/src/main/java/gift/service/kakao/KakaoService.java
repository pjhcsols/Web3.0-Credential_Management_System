package gift.service.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.controller.kakao.KakaoProperties;
import gift.domain.order.Order;
import gift.domain.order.OrderRequest;
import gift.domain.product.option.ProductOption;
import gift.domain.user.User;
import gift.domain.user.UserInfoDto;
import gift.repository.order.OrderRepository;
import gift.repository.product.option.ProductOptionRepository;
import gift.repository.wish.WishRepository;
import gift.service.order.OrderProcessingService;
import gift.service.point.PointService;
import gift.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import gift.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoService {
    private static final Logger logger = LoggerFactory.getLogger(KakaoService.class);

    @Value("${point.earn.rate}")
    private double pointEarnRate;

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final KakaoProperties kakaoProperties;
    private final UserService userService;
    private final ProductOptionRepository productOptionRepository;
    private final WishRepository wishRepository;
    private final OrderRepository orderRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PointService pointService;
    private final OrderProcessingService orderProcessingService;

    @Autowired
    public KakaoService(KakaoProperties kakaoProperties,
                        UserService userService,
                        ProductOptionRepository productOptionRepository,
                        WishRepository wishRepository,
                        OrderRepository orderRepository,
                        JwtTokenUtil jwtTokenUtil,
                        PointService pointService,
                        OrderProcessingService orderProcessingService) {
        this.client = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.kakaoProperties = kakaoProperties;
        this.userService = userService;
        this.productOptionRepository = productOptionRepository;
        this.wishRepository = wishRepository;
        this.orderRepository = orderRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.pointService = pointService;
        this.orderProcessingService = orderProcessingService;
    }

    public UserInfoDto kakaoLogin(String authorizationCode) {
        logger.info("kakaoLogin 시작 - authorizationCode: {}", authorizationCode);
        String tokenUrl = kakaoProperties.tokenUrl();
        String clientId = kakaoProperties.clientId();
        String redirectUri = kakaoProperties.redirectUri();

        var body = createTokenRequestBody(clientId, redirectUri, authorizationCode);

        try {
            var tokenResponse = requestAccessToken(tokenUrl, body);
            String accessToken = extractAccessToken(tokenResponse);
            logger.info("Access token 수신: {}", accessToken);

            UserInfoDto userInfoDto = getUserInfoDto(accessToken);

            Long id = userInfoDto.getId();
            String email = userInfoDto.getEmail();
            logger.info("사용자 정보 조회: id={}, email={}", id, email);

            User user = userService.findOrCreateUser(id, email);
            logger.info("사용자 조회 또는 생성: id={}, email={}", user.getId(), user.getEmail());

            Map<String, String> tokens = userService.generateJwtToken(user);

            // Update userInfoDto with additional information
            userInfoDto = new UserInfoDto(
                    userInfoDto.getId(),
                    userInfoDto.getNickname(),
                    userInfoDto.getEmail(),
                    accessToken,
                    tokens.get("jwt_token"),
                    tokens.get("jwt_refresh"),
                    user.getId(),
                    user.getEmail()
            );

            logger.info("kakaoLogin 성공 - 사용자: {}", user.getId());
            return userInfoDto;

        } catch (RestClientException e) {
            logger.error("요청 실패", e);
            throw new RuntimeException("요청 실패", e);
        }
    }

    private UserInfoDto getUserInfoDto(String accessToken) {
        logger.info("Access token을 사용하여 사용자 정보 가져오기: {}", accessToken);
        String userInfoUrl = kakaoProperties.userInfoUrl();

        try {
            String response = client.post()
                    .uri(URI.create(userInfoUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            logger.debug("사용자 정보 응답: {}", response);

            long id = jsonNode.path("id").asLong();
            JsonNode properties = jsonNode.path("properties");
            String nickname = properties.path("nickname").asText();
            JsonNode kakaoAccount = jsonNode.path("kakao_account");
            String email = kakaoAccount.path("email").asText();

            return new UserInfoDto(id, nickname, email);

        } catch (RestClientResponseException e) {
            logger.error("사용자 정보 가져오기 실패: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("사용자 정보 가져오기 실패", e);
        } catch (Exception e) {
            logger.error("사용자 정보 가져오기 실패", e);
            throw new RuntimeException("사용자 정보 가져오기 실패", e);
        }
    }

    private LinkedMultiValueMap<String, String> createTokenRequestBody(String clientId, String redirectUri, String authorizationCode) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);
        return body;
    }

    private String requestAccessToken(String url, LinkedMultiValueMap<String, String> body) {
        try {
            logger.info("Access token 요청 - body: {}", body);
            var response = client.post()
                    .uri(URI.create(url))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            logger.debug("Access token 응답: {}", response);
            return response;
        } catch (RestClientResponseException e) {
            logger.error("Access token 요청 실패", e);
            throw new RuntimeException("Access token 요청 실패", e);
        }
    }

    private String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();
            logger.debug("Access token 추출: {}", accessToken);
            return accessToken;
        } catch (Exception e) {
            logger.error("Access token 추출 실패", e);
            throw new RuntimeException("Access token 추출 실패", e);
        }
    }

    @Transactional
    public Map<String, Object> createKakaoOrder(User user, String accessToken, OrderRequest orderRequest) {
        Order order = orderProcessingService.processOrder(user, orderRequest);

        sendKakaoMessage(user, accessToken, orderRequest.getMessage(), order.getProductOption().getName(), orderRequest.getQuantity());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedNow = now.format(formatter);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("optionId", orderRequest.getOptionId());
        response.put("quantity", orderRequest.getQuantity());
        response.put("orderDateTime", formattedNow);
        response.put("message", orderRequest.getMessage());
        response.put("remainingCashAmount", order.getRemainingCashAmount());
        response.put("pointsToUse", order.getPointsToUse());
        response.put("totalPoints", pointService.getUserPoints(user));

        logger.info("주문 완료: {}", response);
        return response;
    }


    private void sendKakaoMessage(User user, String accessToken, String message, String optionName, Long quantity) {
        String messageUrl = kakaoProperties.messageUrl();

        var body = new LinkedMultiValueMap<String, String>();
        body.add("template_object", createTemplateObject(user, message, optionName, quantity));

        try {
            client.post()
                    .uri(URI.create(messageUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Kakao 메시지 전송 실패", e);
        }
    }

    private String createTemplateObject(User user, String message, String optionName, Long quantity) {
        Map<String, Object> templateObject = new HashMap<>();
        templateObject.put("object_type", "text");
        templateObject.put("text", String.format("주문 접수: %s\n옵션: %s\n수량: %d\n메시지: %s",
                user.getEmail(), optionName, quantity, message));
        templateObject.put("link", Map.of("web_url", "http://your-web-url.com"));

        try {
            return objectMapper.writeValueAsString(templateObject);
        } catch (Exception e) {
            throw new RuntimeException("템플릿 객체 생성 실패", e);
        }
    }
}
