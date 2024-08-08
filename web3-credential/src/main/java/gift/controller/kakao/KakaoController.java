package gift.controller.kakao;

import gift.domain.order.OrderRequest;
import gift.domain.user.User;
import gift.domain.user.UserInfoDto;
import gift.validation.LoginMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gift.service.kakao.KakaoService;

import java.net.URI;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/kakao")
public class KakaoController {

    private final KakaoService kakaoService;

    private final KakaoProperties kakaoProperties;

    @Autowired
    public KakaoController(KakaoService kakaoService, KakaoProperties kakaoProperties) {
        this.kakaoService = kakaoService;
        this.kakaoProperties = kakaoProperties;
    }

    @Operation(summary = "카카오 로그인", description = "카카오 로그인 코드로 사용자를 인증합니다.")
    @PostMapping("/login")
    public UserInfoDto kakaoLogin(
            @Parameter(description = "카카오 인증 코드", required = true)
            @RequestParam("code") String authorizationCode) {
        return kakaoService.kakaoLogin(authorizationCode);
    }

    @Operation(summary = "카카오 주문 생성", description = "카카오 API를 통해 주문을 생성합니다.")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createOrder(
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser,
            @Parameter(description = "카카오 접근 토큰", required = true)
            @RequestParam("accessToken") String accessToken,
            @Parameter(description = "주문 요청 정보", required = true)
            @RequestBody OrderRequest orderRequest) {
        Map<String, Object> response = kakaoService.createKakaoOrder(loginUser, accessToken, orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/code")
    public ResponseEntity<Void> signInKakaoRedirectUrl() {
        String redirectUrl = UriComponentsBuilder.fromUriString(kakaoProperties.codeUrl())
                .queryParam("scope", "talk_message")
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", kakaoProperties.redirectUri())
                .queryParam("client_id", kakaoProperties.clientId())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

