package com.event.service;

import com.event.exception.CustomEventException;
import com.event.mapper.EventMapper;
import com.event.model.dto.EventDto;
import com.event.model.entity.EventEntity;
import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService 단위 테스트")
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Nested
    @DisplayName("selectEventList")
    class SelectEventListTest {
        @Test
        @DisplayName("검색어와 지역이 주어졌을 때 필터링된 이벤트 목록을 반환한다")
        void givenQueryAndArea_whenSelectEventList_thenReturnsFilteredPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            String query = "축제";
            String area = "서울,부산";

            EventEntity eventEntity = new EventEntity();
            EventEntity eventEntity2 = new EventEntity();
            Page<EventEntity> eventEntityPage = new PageImpl<>(List.of(eventEntity, eventEntity2));

            EventListResponse eventListResponse = new EventListResponse(1L, "행사1", "서울", null, null, null);
            EventListResponse eventListResponse2 = new EventListResponse(2L, "행사2", "부산", null, null, null);

            given(eventRepository.findAll(ArgumentMatchers.<Specification<EventEntity>>any(), eq(pageable)))
                    .willReturn(eventEntityPage);
            given(eventMapper.toEventListResponse(eventEntity)).willReturn(eventListResponse);
            given(eventMapper.toEventListResponse(eventEntity2)).willReturn(eventListResponse2);

            // When
            Page<EventListResponse> eventListResponsePage = eventService.selectEventList(pageable, query, area);

            // Then
            assertThat(eventListResponsePage).hasSize(2);
            assertThat(eventListResponsePage.getContent().getFirst().area()).isEqualTo("서울");

            then(eventRepository).should().findAll(ArgumentMatchers.<Specification<EventEntity>>any(), eq(pageable));
            then(eventMapper).should(times(2)).toEventListResponse(any(EventEntity.class));
        }
    }

    @Nested
    @DisplayName("selectEvent")
    class SelectEventTest {
        @Test
        @DisplayName("존재하는 contentId로 이벤트 조회 시 상세 정보를 반환한다")
        void givenValidContentId_whenSelectEvent_thenReturnsEventResponse() {
            // Given
            Long contentId = 1L;
            EventEntity eventEntity = new EventEntity();
            EventResponse eventResponse = new EventResponse(
                    1L, "행사 제목", LocalDateTime.now(), LocalDateTime.now(),
                    "주소1", "주소2", "서울", null, null,
                    0.0, 0.0, "12345", "homepage", "설명",
                    null, null, "시간", "이용시간", "주최1", "010", "주최2", "020", Instant.now());

            given(eventRepository.findById(contentId)).willReturn(Optional.of(eventEntity));
            given(eventMapper.toEventResponse(eventEntity)).willReturn(eventResponse);

            // When
            EventResponse result = eventService.selectEvent(contentId);

            // Then
            assertThat(result.contentId()).isEqualTo(contentId);

            then(eventRepository).should().findById(contentId);
            then(eventMapper).should().toEventResponse(eventEntity);
        }

        @Test
        @DisplayName("존재하지 않는 contentId로 조회 시 예외 발생")
        void givenInvalidContentId_whenSelectEvent_thenThrowsException() {
            // Given
            Long contentId = 999L;
            given(eventRepository.findById(contentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> eventService.selectEvent(contentId))
                    .isInstanceOf(CustomEventException.class);

            then(eventRepository).should().findById(contentId);
            then(eventMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("upsertEvent")
    class UpsertEventTest {

        @Test
        @DisplayName("이벤트 업서트 시 DB 저장 또는 갱신")
        void givenEventDto_whenUpsertEvent_thenSavesEntity() {
            // Given
            EventDto eventDto = new EventDto();
            EventEntity eventEntity = new EventEntity();

            given(eventMapper.toEventEntity(eventDto)).willReturn(eventEntity);
            given(eventRepository.save(eventEntity)).willReturn(eventEntity);

            // When
            eventService.upsertEvent(eventDto);

            // Then
            then(eventMapper).should().toEventEntity(eventDto);
            then(eventRepository).should().save(eventEntity);
        }
    }

}
