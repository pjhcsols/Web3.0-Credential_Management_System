package web3.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import web3.properties.JwtProperties;
import web3.properties.KakaoProperties;

import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

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
                    .setSigningKey(jwtProperties.key().getBytes())
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
                .setSigningKey(jwtProperties.key().getBytes())
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
                .signWith(Keys.hmacShaKeyFor(jwtProperties.key().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}
