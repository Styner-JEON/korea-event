package com.event.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정 프로퍼티를 관리하는 클래스
 *
 * application.yml의 jwt 프리픽스로 시작하는
 * 설정값들을 자동으로 바인딩하여 JWT 토큰 처리에 필요한 설정을 제공합니다.
 */
@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
@Getter
public class JwtProperties {

    private final String secretString;

}
