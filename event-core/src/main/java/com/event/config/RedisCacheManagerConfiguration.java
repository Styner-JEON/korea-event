package com.event.config;

import com.event.model.response.CommentAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 캐시 매니저 설정 클래스
 * <p>
 * Spring Cache를 사용해서 Redis를 캐쉬 저장소로 사용하기 위한 설정을 제공합니다.
 * 댓글 분석 결과(CommentAnalysisResponse)를 캐슁합니다.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
@Profile("!test")
public class RedisCacheManagerConfiguration {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // CommentAnalysisResponse 전용 직렬화기 생성
        Jackson2JsonRedisSerializer<CommentAnalysisResponse> commentAnalysisResponseSerializer = new Jackson2JsonRedisSerializer<>(
                objectMapper, CommentAnalysisResponse.class);

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues() // null 값은 캐시하지 않음
                .serializeKeysWith(
                        // 키는 문자열로 직렬화
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        // 값은 JSON으로 직렬화
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(commentAnalysisResponseSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

}
