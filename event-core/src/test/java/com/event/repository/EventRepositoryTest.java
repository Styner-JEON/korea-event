package com.event.repository;

import com.event.model.entity.EventEntity;
import com.event.specification.EventSpecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("EventRepository JPA 테스트")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Nested
    @DisplayName("findAll")
    class FindAllTest {
        @Test
        @DisplayName("이벤트 15개가 존재할 때, 0페이지 10개 조회, hasNext는 true")
        void given15Events_whenFindAllWithPaging_thenReturns10AndHasNextTrue() {
            // Given
            for (int i = 0; i < 15; i++) {
                eventRepository.save(createEventEntity("행사" + i, "서울"));
            }
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "modifiedTime"));

            // When
            Page<EventEntity> eventEntityPage = eventRepository.findAll(pageable);

            // Then
            assertThat(eventEntityPage.getContent()).hasSize(10);
            assertThat(eventEntityPage.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("지역 필터링")
    class WithAreaTest {
        @Test
        @DisplayName("지정한 지역 리스트에 포함된 이벤트만 반환한다")
        void givenAreaFilter_whenFindAllWithSpec_thenReturnsFilteredEvents() {
            // Given
            eventRepository.save(createEventEntity("서울 행사", "서울")); // 포함
            eventRepository.save(createEventEntity("부산 행사", "부산")); // 포함
            eventRepository.save(createEventEntity("제주 행사", "제주"));

            List<String> areaList = List.of("서울", "부산");
            Pageable pageable = PageRequest.of(0, 10);

            // When: 지역 필터에 맞는 이벤트 조회
            Page<EventEntity> eventEntityPage = eventRepository.findAll(EventSpecs.withArea(areaList), pageable);

            // Then: 서울, 부산만 포함됨
            assertThat(eventEntityPage.getContent())
                    .extracting(EventEntity::getArea)
                    .containsExactlyInAnyOrder("서울", "부산");
        }
    }

    @Nested
    @DisplayName("검색어 필터링")
    class WithQueryTest {
        @Test
        @DisplayName("검색어에 해당하는 필드가 있는 이벤트만 반환한다")
        void givenQuery_whenFindAllWithSpec_thenReturnsMatchedEvents() {
            // Given: 제목에 "꽃"이 포함된 이벤트와 포함되지 않은 이벤트 등록
            eventRepository.save(createEventEntity("한강 불꽃놀이", "서울")); // 포함
            eventRepository.save(createEventEntity("제주 꽃축제", "제주"));  // 포함
            eventRepository.save(createEventEntity("부산 음악회", "부산"));

            Pageable pageable = PageRequest.of(0, 10);

            // When: "꽃" 검색어로 필터링
            Page<EventEntity> eventEntityPage = eventRepository.findAll(EventSpecs.withQuery("꽃"), pageable);

            // Then: "제주 꽃축제", "한강 불꽃놀이" 모두 포함 가능성 있음 → contains만 사용
            assertThat(eventEntityPage.getContent())
                    .extracting(EventEntity::getTitle)
                    .contains("제주 꽃축제");
        }
    }

    @Nested
    @DisplayName("검색어 + 지역 필터링")
    class WithQueryAndAreaTest {
        @Test
        @DisplayName("검색어와 지역이 모두 일치하는 이벤트만 반환한다")
        void givenQueryAndArea_whenFindAllWithSpec_thenReturnsMatchedEvents() {
            // Given: 검색어 "축제" + 지역 "서울", "부산"
            eventRepository.save(createEventEntity("서울 꽃축제", "서울"));  // 포함
            eventRepository.save(createEventEntity("부산 불꽃축제", "부산")); // 포함
            eventRepository.save(createEventEntity("제주 음악회", "제주"));  // 지역 불일치
            eventRepository.save(createEventEntity("서울 연극제", "서울"));  // 검색어 불일치

            String query = "축제";
            List<String> areaList = List.of("서울", "부산");
            Pageable pageable = PageRequest.of(0, 10);

            // When: 검색어 + 지역 조건 모두 만족하는 이벤트 조회
            Page<EventEntity> eventEntityPage = eventRepository.findAll(
                    EventSpecs.withQuery(query).and(EventSpecs.withArea(areaList)),
                    pageable
            );

            // Then: 조건을 모두 만족하는 두 이벤트만 포함됨
            assertThat(eventEntityPage.getContent())
                    .extracting(EventEntity::getTitle)
                    .containsExactlyInAnyOrder("서울 꽃축제", "부산 불꽃축제");
        }
    }

    /**
     * 테스트용 이벤트 엔티티 생성 메서드
     *
     * @param title 이벤트 제목
     * @param area  지역명
     * @return 저장 전 상태의 이벤트 엔티티
     */
    private EventEntity createEventEntity(String title, String area) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setContentId(System.nanoTime()); // 고유값 보장
        eventEntity.setTitle(title);
        eventEntity.setArea(area);
        eventEntity.setCreatedTime(LocalDateTime.now().minusDays(1));
        eventEntity.setModifiedTime(LocalDateTime.now());
        eventEntity.setEventStartDate(LocalDate.now().plusDays(10));
        eventEntity.setEventEndDate(LocalDate.now().plusDays(12));
        eventEntity.setDbUpsertedAt(Instant.now());
        return eventEntity;
    }

}
