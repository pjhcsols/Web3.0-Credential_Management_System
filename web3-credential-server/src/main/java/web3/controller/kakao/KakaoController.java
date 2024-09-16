package web3.controller.kakao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import web3.properties.KakaoProperties;
import web3.domain.user.User;
import web3.service.dto.user.UserInfoDto;
import web3.service.kakao.KakaoService;
import web3.validation.LoginMember;

import java.net.URI;
import java.util.Map;

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

    @Operation(summary = "카카오 지갑 정보 전송", description = "카카오 API를 통해 지갑 정보를 전송합니다.")
    @PostMapping("/wallets")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> sendWalletInfo(
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser,
            @Parameter(description = "카카오 접근 토큰", required = true)
            @RequestParam("accessToken") String accessToken) {
        Map<String, Object> response = kakaoService.sendWalletInfo(loginUser, accessToken);
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

