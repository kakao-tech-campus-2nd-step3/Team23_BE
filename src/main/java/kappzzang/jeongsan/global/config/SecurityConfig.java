package kappzzang.jeongsan.global.config;

import kappzzang.jeongsan.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .formLogin((config) -> config.disable())
            .httpBasic((config) -> config.disable())
            .csrf((config) -> config.disable())
            .sessionManagement((config) -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((registry) -> registry
                // 해당 경로는 인증 없이 접근 허용
                .requestMatchers("/api/members/token")
                .permitAll()
                .anyRequest()
                .authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(authenticationManagerBuilder.getOrBuild()), LogoutFilter.class)
            .build();
    }
}
