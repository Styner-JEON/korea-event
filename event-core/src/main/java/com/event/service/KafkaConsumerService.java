package com.event.service;

import com.event.model.dto.EventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka 메시지를 소비하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EventService eventService;

    /**
     * Kafka 토픽에서 이벤트 메시지를 소비합니다.
     * 
     * bridge-service에서 전송한 이벤트 데이터를 수신하여
     * EventService를 통해 데이터베이스에 저장합니다.
     * 
     * @param eventDto Kafka로부터 수신한 이벤트 데이터
     */
    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}", autoStartup = "${spring.kafka.consumer.auto-startup}")
    public void consumeEvent(EventDto eventDto) {
        log.info("Consumed contentId: {}", eventDto.getContentId());
        eventService.insertEvent(eventDto);
    }

}
