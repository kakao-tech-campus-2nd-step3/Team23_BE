package kappzzang.jeongsan.global.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.global.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final List<String> permittedPaths;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // Authorization에서 JWT 추출
        String token = jwtUtil.resolveToken(request);

        // JWT로 인증된 Authentication을 SecurityContextHolder에 저장
        if (!isPathPermitted(request.getRequestURI()) && StringUtils.hasText(token)) {
            try {
                Authentication jwtAuthenticationToken = new JwtAuthenticationToken(token);
                Authentication authentication = authenticationManager.authenticate(
                    jwtAuthenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JeongsanException jeongsanException) {
                log.error(jeongsanException.getMessage());
                handleJwtException(response, jeongsanException);
                SecurityContextHolder.clearContext();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPathPermitted(String requestPath) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return permittedPaths.stream().anyMatch(path -> pathMatcher.match(path, requestPath));
    }

    private void handleJwtException(HttpServletResponse response,
        JeongsanException jeongsanException) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "failure");
        errorResponse.put("errorCode", jeongsanException.getErrorType().getErrorCode());
        errorResponse.put("message", jeongsanException.getMessage());

        response.setStatus(jeongsanException.getErrorType().getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
