package kappzzang.jeongsan.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // Authorization에서 JWT 추출
        String token = resolveToken(request);

        // JWT로 인증
        if (StringUtils.hasText(token)) {
            try {
                Authentication authentication = new JwtAuthenticationToken(token);
                authentication = authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException authenticationException) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
