package com.bridge.service;

import com.bridge.exception.CustomKafkaException;
import com.bridge.model.dto.EventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 카프카 이벤트 메시지 프로듀서
 *
 * 수집된 이벤트 데이터를 카프카으로 전송하는 메시지 프로듀서입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, EventDto> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String topic;

    @Value("${spring.kafka.timeout-seconds}")
    private long timeoutSeconds;

    /**
     * EventDto 객체를 JSON 형태로 직렬화하여 설정된 카프카로 전송합니다.
     * 동기식 전송을 사용하여 전송 결과를 즉시 확인하고 성공/실패를 처리합니다.
     * 
     * @param eventDto   카프카로 전송할 이벤트 DTO 객체
     * @throws CustomKafkaException 카프카 전송 실패 시 발생하는 커스텀 예외
     */
    public void sendEventDto(EventDto eventDto) {
        // 전송 결과를 담을 변수 초기화
        SendResult<String, EventDto> sendResult = null;

        try {
            sendResult = kafkaTemplate
                    .send(topic, eventDto)
                    .get(timeoutSeconds, TimeUnit.SECONDS);  // 동기식 전송으로 결과를 기다림 (타임아웃 적용)

            // 전송 성공 시 메타데이터 추출 및 로깅
            RecordMetadata metadata = sendResult.getRecordMetadata();
            log.info("Kafka message sent successfully. Topic: {}, Partition: {}, Offset: {}, contentId: {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), eventDto.getContentId());
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            // 전송 실패 시 예외 처리
            log.error("Kafka message sending failed (contentId: {}): {}", eventDto.getContentId(), e.getMessage(), e);
            throw new CustomKafkaException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Kafka message sending failed (contentId: %s): %s", eventDto.getContentId(),
                            e.getMessage())
            );
        }
    }

}
