package kappzzang.jeongsan.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import kappzzang.jeongsan.global.common.security.JwtAuthenticationFilter;
import kappzzang.jeongsan.global.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final ObjectMapper objectMapper;

    @Value("${security.permitted-paths}")
    private List<String> permittedPaths;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                (config) -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((registry) -> registry
                .requestMatchers(permittedPaths.toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .addFilterAfter(
                new JwtAuthenticationFilter(jwtUtil, authenticationManagerBuilder.getOrBuild(),
                    permittedPaths, objectMapper), LogoutFilter.class)
            .build();
    }
}
