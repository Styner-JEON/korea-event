package com.event.service;

import com.event.exception.CustomEventException;
import com.event.mapper.EventMapper;
import com.event.model.dto.EventDto;
import com.event.model.entity.EventEntity;
import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.repository.EventRepository;
import com.event.specification.EventSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 이벤트 관련 비즈니스 로직을 처리하는 서비스
 * 
 * 이벤트 등록, 조회, 검색 기능을 제공하며
 * Kafka로부터 전달받은 이벤트 데이터를 저장하는 역할을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventMapper eventMapper;

    private final EventRepository eventRepository;

    /**
     * 이벤트를 데이터베이스에 upsert 합니다.
     * 동일한 contentId가 존재하면 UPDATE, 없으면 INSERT 됩니다.
     * 
     * @param eventDto 저장할 이벤트 정보
     */
    @Transactional
    public void upsertEvent(EventDto eventDto) {
        EventEntity eventEntity = eventMapper.toEventEntity(eventDto);
        eventEntity.setDbUpsertedAt(Instant.now());

        eventRepository.save(eventEntity);
        log.info("DB upsert completed for contentId: {}", eventDto.getContentId());
    }

//     @Transactional(readOnly = true)
//     public Page<EventListResponse> selectEventList(Pageable pageable, String query) {
//         Page<EventEntity> eventEntitiyPage;
//         if (query == null || query.trim().isEmpty()) {
//            eventEntitiyPage = eventRepository.findAll(pageable);
//         } else {
//            eventEntitiyPage = eventRepository.searchEvents(pageable, query);
//         }
//
//         return eventEntitiyPage.map(eventMapper::toEventListResponse);
//     }

    /**
     * 이벤트 목록을 조회합니다.
     * 검색어와 지역 필터를 통해 조건부 검색이 가능합니다.
     * 
     * @param pageable 페이지네이션 정보
     * @param query    검색어 (선택사항)
     * @param area     지역 필터 (쉼표로 구분된 지역 목록, 선택사항)
     * @return 이벤트 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<EventListResponse> selectEventList(Pageable pageable, String query, String area) {
        List<String> areaList = null;
        if (area != null && !area.trim().isEmpty()) {
            areaList = Arrays.asList(area.split(","));
        }

        Specification<EventEntity> spec = EventSpecs.withQuery(query).and(EventSpecs.withArea(areaList));
        Page<EventEntity> eventEntitiyPage = eventRepository.findAll(spec, pageable);
        return eventEntitiyPage.map(eventMapper::toEventListResponse);
    }

    /**
     * 특정 이벤트의 상세 정보를 조회합니다.
     * 
     * @param contentId 조회할 이벤트의 컨텐츠 ID
     * @return 이벤트 상세 정보
     * @throws CustomEventException 해당 ID의 이벤트가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public EventResponse selectEvent(Long contentId) {
        EventEntity eventEntity = eventRepository.findById(contentId).orElseThrow(() -> {
            log.debug("Event not found with contentId: {}", contentId);
            return new CustomEventException(HttpStatus.NOT_FOUND, "Event not found with contentId: " + contentId);
        });
        return eventMapper.toEventResponse(eventEntity);
    }

}
