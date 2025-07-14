package com.event.service;

import com.event.exception.CustomEventException;
import com.event.mapper.EventMapper;
import com.event.model.dto.EventDto;
import com.event.model.entity.EventEntity;
import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventMapper eventMapper;

    private final EventRepository eventRepository;

    @Transactional
    public void insertEvent(EventDto eventDto) {
        EventEntity eventEntity = eventMapper.toEventEntity(eventDto);
        eventEntity.setDbUpdatedAt(LocalDateTime.now());

        eventRepository.save(eventEntity);
        log.info("DB insert completed for contentId: {}", eventDto.getContentId());
    }

    @Transactional(readOnly = true)
    public Page<EventListResponse> selectEventList(Pageable pageable, String query) {
        Page<EventEntity> eventEntitiyPage;
        if (query == null || query.trim().isEmpty()) {
            eventEntitiyPage = eventRepository.findAll(pageable);
        } else {
            eventEntitiyPage = eventRepository.searchEvents(pageable, query);
        }
        return eventEntitiyPage.map(eventMapper::toEventListResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse selectEvent(Long contentId) {
        EventEntity eventEntity = eventRepository.findById(contentId).orElseThrow(() -> {
            log.debug("Event not found with contentId: {}", contentId);
            return new CustomEventException(HttpStatus.NOT_FOUND, "Event not found with contentId: " + contentId);
        });
        return eventMapper.toEventResponse(eventEntity);
    }

}
