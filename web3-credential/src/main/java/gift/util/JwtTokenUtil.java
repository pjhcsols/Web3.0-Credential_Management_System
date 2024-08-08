package gift.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtTokenUtil {

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityMilliseconds;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityMilliseconds;

    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenValidityMilliseconds);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenValidityMilliseconds);
    }

    public void blacklistToken(String token) {
        // Implement logic to invalidate/blacklist token if needed
    }

    public boolean validateToken(String token) {
        try {

            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token has expired");
            return false;
        } catch (Exception e) {
            System.out.println("Invalid token: " + e.getMessage());
            return false;
        }
    }


    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(String email, long validityMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
