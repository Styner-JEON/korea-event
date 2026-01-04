package com.event.security;

import com.event.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 설정 클래스
 *
 * 다른 도메인에서의 API 요청을 허용하기 위한 설정을 담당합니다
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진(도메인) 설정
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        // 프론트에서 사용하는 메서드 + preflight(OPTIONS) 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // 자격 증명(쿠키, 인증 헤더 등) 포함 요청 허용
        configuration.setAllowCredentials(true);
        // URL 패턴별 CORS 설정을 관리하는 소스 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로("/**")에 대해 위에서 설정한 CORS 정책 적용
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
