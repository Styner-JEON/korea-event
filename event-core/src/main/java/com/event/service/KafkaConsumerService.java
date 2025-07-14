package com.event.service;

import com.event.model.dto.EventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final EventService eventService;

    @KafkaListener(
            topics = "${spring.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            autoStartup = "${spring.kafka.consumer.auto-startup}"
    )
    public void consumeEvent(EventDto eventDto) {
        log.info("Consumed contentId: {}", eventDto.getContentId());
        eventService.insertEvent(eventDto);
    }

}

