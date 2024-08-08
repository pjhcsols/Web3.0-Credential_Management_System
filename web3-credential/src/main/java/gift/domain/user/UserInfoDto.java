package gift.domain.user;

public class UserInfoDto {
    private long id;
    private String nickname;
    private String email;
    private String accessToken;
    private String jwtToken;
    private String refreshToken;
    private Long serverUserId;
    private String serverUserEmail;

    public UserInfoDto(long id, String nickname, String email, String accessToken,
                       String jwtToken, String refreshToken, Long serverUserId,
                       String serverUserEmail) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.accessToken = accessToken;
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
        this.serverUserId = serverUserId;
        this.serverUserEmail = serverUserEmail;
    }

    public UserInfoDto(long id, String nickname, String email) {
        this(id, nickname, email, "", "", "", 0L, "");
    }
    // Getters only
    public long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getServerUserId() {
        return serverUserId;
    }

    public String getServerUserEmail() {
        return serverUserEmail;
    }
}
