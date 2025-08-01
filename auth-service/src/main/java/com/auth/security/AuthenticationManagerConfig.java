package com.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 인증 관리자 설정 클래스
 *
 * Spring Security의 AuthenticationManager를 구성합니다.
 */
@Configuration
public class AuthenticationManagerConfig {

    /**
     * 사용자 인증을 처리하는 AuthenticationManager를 설정합니다.
     * DaoAuthenticationProvider를 사용하여 데이터베이스 기반 인증을 수행합니다.
     * 
     * @param customUserDetailsService 사용자 정보 로드 서비스
     * @param passwordEncoder          비밀번호 암호화 인코더
     * @return 구성된 AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

}
