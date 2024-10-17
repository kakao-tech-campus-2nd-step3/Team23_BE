package kappzzang.jeongsan.global.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
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
        try {
            return Long.parseLong(Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
        } catch (SignatureException signatureException) {
            throw new JeongsanException(ErrorType.JWT_SIGNATURE_INVALID);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JeongsanException(ErrorType.JWT_EXPIRED);
        } catch (MalformedJwtException malformedJwtException) {
            throw new JeongsanException(ErrorType.JWT_MALFORMED);
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String createAccessToken(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
            .subject(Long.toString(id))
            .claim("iat", createIssueAt(now))
            .claim("exp", createExpiration(now, jwtProperties.accessExpirationTime()))
            .signWith(secretKey)
            .compact();
    }

    public String createRefreshToken() {
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
            .claim("iat", createIssueAt(now))
            .claim("exp", createExpiration(now, jwtProperties.refreshExpirationTime()))
            .signWith(secretKey)
            .compact();
    }

    private long createIssueAt(LocalDateTime now) {
        return now.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    private long createExpiration(LocalDateTime now, long expirationTime) {
        return now.plus(expirationTime, ChronoUnit.MILLIS)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond();
    }
}
