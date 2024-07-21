package com.toy.project.emodiary.authentication.config;

import com.toy.project.emodiary.authentication.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtExceptionFilter jwtExceptionFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // HTTP 기본 인증 사용하지 않음
                .httpBasic(AbstractHttpConfigurer::disable)

                // CSRF 사용하지 않음
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 사용하지 않음
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패
                        .accessDeniedHandler(jwtAccessDeniedHandler)) // 권한 없음

                // 권한 설정
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/error").permitAll()
                        // 회원
                        .requestMatchers("/auth/signup/**").permitAll()
                        .requestMatchers("/auth/signin/**").permitAll()
                        .requestMatchers("/auth/token/access").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/users/password").permitAll()
                        .requestMatchers("/auth/users/password/reset/**").permitAll()

                        // S3
                        .requestMatchers("/s3/upload/**").permitAll()

                        // 날씨 API
                        .requestMatchers("/weather/**").permitAll()

                        // 그 외
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터(jwt가 유효할 경우 UsernamePasswordAuthenticationFilter는 실행되지 않음)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                // JWT 예외 처리 필터
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
