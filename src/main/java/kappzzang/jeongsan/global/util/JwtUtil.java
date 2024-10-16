package kappzzang.jeongsan.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.secretKey()));
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public Long getMemberId(String token) {
        return Long.parseLong(Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject());
    }

    public String createAccessToken(Long id) {
        Date now = new Date();
        return Jwts.builder()
            .subject(Long.toString(id))
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtProperties.accessExpirationTime()))
            .signWith(secretKey)
            .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
            .issuedAt(now)
            .expiration(new Date(now.getTime() + jwtProperties.refreshExpirationTime()))
            .signWith(secretKey)
            .compact();
    }
}
