package kappzzang.jeongsan.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret-key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
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
}
