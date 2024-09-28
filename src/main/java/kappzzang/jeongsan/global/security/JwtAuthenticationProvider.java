package kappzzang.jeongsan.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import javax.crypto.SecretKey;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final SecretKey secretKey;

    public JwtAuthenticationProvider(@Value("${jwt.secret-key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
    }

    // JWT를 파싱하여 Authentication 생성
    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(((JwtAuthenticationToken) authentication).getToken())
                .getPayload();
            // 사용자 role은 아직 없음
            return new JwtAuthenticationToken(claims.getSubject(), null, null);
        } catch (SignatureException signatureException) {
            throw new JeongsanException(ErrorType.JWT_SIGNATURE_INVALID);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JeongsanException(ErrorType.JWT_EXPIRED);
        } catch (MalformedJwtException malformedJwtException) {
            throw new JeongsanException(ErrorType.JWT_MALFORMED);
        }
    }

    // filter로부터 받은 AuthenticationToken이 해당 provider가 지원하는 타입인지 확인
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
