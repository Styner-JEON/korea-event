package com.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정 클래스
 *
 * 비밀번호 암호화에 사용할 PasswordEncoder를 설정합니다.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt 해시 함수를 사용하여 비밀번호를 안전하게 암호화합니다.
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
