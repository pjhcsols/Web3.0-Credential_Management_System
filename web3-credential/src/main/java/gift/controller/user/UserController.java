package gift.controller.user;

import gift.domain.user.User;
import gift.exception.user.InvalidCredentialsException;
import gift.exception.user.UserAlreadyExistsException;
import gift.exception.user.UserNotFoundException;
import gift.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "로그인 요청 정보", required = true)
            @RequestBody LoginRequest loginRequest) throws UserNotFoundException, InvalidCredentialsException {
        String accessToken = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        String refreshToken = userService.generateRefreshToken(loginRequest.getEmail());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    @Operation(summary = "모든 사용자 조회", description = "모든 사용자를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<User>> findAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "사용자 등록 요청 정보", required = true)
            @RequestBody RegisterRequest registerRequest) throws UserAlreadyExistsException {
        Map<String, String> tokens = userService.registerUser(registerRequest.getEmail(), registerRequest.getPassword());
        return ResponseEntity.ok(tokens);
    }
}



class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class RegisterRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class JwtResponse {
    private String accessToken;
    private String refreshToken;

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
