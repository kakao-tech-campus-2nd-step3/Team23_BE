package kappzzang.jeongsan.global.security;

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
        String token = ((JwtAuthenticationToken) authentication).getToken();
        Long memberId = jwtUtil.getMemberId(token);

        // 사용자 role은 아직 없음
        return new JwtAuthenticationToken(memberId, null, null);
    }

    // filter로부터 받은 AuthenticationToken이 해당 provider가 지원하는 타입인지 확인
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
