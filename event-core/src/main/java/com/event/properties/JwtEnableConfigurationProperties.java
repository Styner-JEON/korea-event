package com.event.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정을 관리하는 Configuration 클래스
 *
 * 이 클래스는 JWT 토큰 처리에 필요한 설정값들을 Spring Boot의
 * Configuration Properties를 통해 관리하도록 설정합니다.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtEnableConfigurationProperties {
}
