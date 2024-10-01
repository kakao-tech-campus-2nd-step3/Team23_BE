package kappzzang.jeongsan.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtUtil jwtUtil;

    // JWT를 파싱하여 Authentication 생성
    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            String token = ((JwtAuthenticationToken) authentication).getToken();
            Long memberId = jwtUtil.getMemberId(token);

            // 사용자 role은 아직 없음
            return new JwtAuthenticationToken(memberId, null, null);
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
