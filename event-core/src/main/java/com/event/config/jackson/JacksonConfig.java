package com.event.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Jackson 직렬화/역직렬화 설정 클래스
 * 
 * 이 클래스는 Spring Boot 애플리케이션에서 JSON 직렬화/역직렬화를 담당하는
 * Jackson ObjectMapper의 전역 설정을 정의합니다.
 * 
 * 주요 기능:
 * - Java 8 시간 API (LocalDate, LocalDateTime) 처리
 * - 날짜/시간 타입의 문자열 형태 직렬화
 * - null 값 필드 제외 설정
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 시간 API 지원을 위한 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();

        // LocalDate 타입에 대한 커스텀 직렬화기 및 역직렬화기 등록
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());

        // LocalDateTime 타입에 대한 커스텀 직렬화기 및 역직렬화기 등록
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

        objectMapper.registerModule(module);

        // 날짜를 타임스탬프가 아닌 문자열 포맷으로 직렬화
        // 이 설정을 통해 날짜가 숫자가 아닌 읽기 쉬운 문자열 형태로 출력
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 값을 가진 필드를 JSON 응답에서 제외
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }
}
