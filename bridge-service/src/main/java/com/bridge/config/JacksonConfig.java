package com.bridge.config;

import com.bridge.deserializer.LocalDateDeserializer;
import com.bridge.deserializer.LocalDateTimeDeserializer;
import com.bridge.serializer.LocalDateSerializer;
import com.bridge.serializer.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    /**
     * LocalDate와 LocalDateTime을 문자열로 직렬화 및 역직렬화
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class,new LocalDateDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(module);

        // Timestamp가 아닌 문자열 포맷 유지
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null인 필드를 JSON에 포함시키지 않음
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }

}
