package gift.domain.user;

public enum Role {
    USER("USER"), ADMIN("ADMIN");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static Role of(String role) {
        if (role.equals("USER")) {
            return USER;
        }
        if (role.equals("ADMIN")) {
            return ADMIN;
        }
        throw new IllegalArgumentException("유효하지 않은 권한입니다.");
    }
}