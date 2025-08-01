package com.bridge.integration;

import com.bridge.controller.BatchController;
import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchController.class)
@DisplayName("Bridge-service 통합 테스트")
class BridgeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicDataApiClient publicDataApiClient;

    @Nested
    @DisplayName("배치 API 조회")
    class BatchApiTest {
        @Test
        @DisplayName("이벤트 데이터가 존재할 때, 이벤트 목록이 정상 조회된다")
        void givenEventDataExists_whenGetBatchApi_thenReturnsEventList() throws Exception {
            // Given
            List<EventDto> mockEventList = List.of(
                    createEventDto(1L, "서울 벚꽃 축제", "서울"),
                    createEventDto(2L, "부산 바다 축제", "부산")
            );

            given(publicDataApiClient.getEventDtoList()).willReturn(mockEventList);

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].contentId").value(1L))
                    .andExpect(jsonPath("$[0].title").value("서울 벚꽃 축제"))
                    .andExpect(jsonPath("$[0].area").value("서울"))
                    .andExpect(jsonPath("$[1].contentId").value(2L))
                    .andExpect(jsonPath("$[1].title").value("부산 바다 축제"))
                    .andExpect(jsonPath("$[1].area").value("부산"));

            then(publicDataApiClient).should().getEventDtoList();
        }

        @Test
        @DisplayName("이벤트 데이터가 존재하지 않을 때, 빈 리스트가 반환된다")
        void givenNoEventData_whenGetBatchApi_thenReturnsEmptyList() throws Exception {
            // Given
            given(publicDataApiClient.getEventDtoList()).willReturn(List.of());

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            then(publicDataApiClient).should().getEventDtoList();
        }

        @Test
        @DisplayName("다양한 지역의 이벤트 데이터가 존재할 때, 모든 이벤트가 정상 조회된다")
        void givenMultipleAreaEvents_whenGetBatchApi_thenReturnsAllEvents() throws Exception {
            // Given
            List<EventDto> mockEventList = List.of(
                    createEventDto(1L, "서울 문화 축제", "서울"),
                    createEventDto(2L, "부산 국제 영화제", "부산"),
                    createEventDto(3L, "제주 감귤 축제", "제주"),
                    createEventDto(4L, "경기 음악 페스티벌", "경기"));

            given(publicDataApiClient.getEventDtoList()).willReturn(mockEventList);

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0].area").value("서울"))
                    .andExpect(jsonPath("$[1].area").value("부산"))
                    .andExpect(jsonPath("$[2].area").value("제주"))
                    .andExpect(jsonPath("$[3].area").value("경기"));

            then(publicDataApiClient).should().getEventDtoList();
        }
    }

    @Nested
    @DisplayName("서비스 통합 테스트")
    class ServiceIntegrationTest {
        @Test
        @DisplayName("PublicDataApiClient 서비스가 정상적으로 호출된다")
        void givenBatchRequest_whenProcessed_thenPublicDataApiClientIsCalled() throws Exception {
            // Given
            List<EventDto> eventDtoList = List.of(createEventDto(1L, "통합 테스트 이벤트", "대전"));

            given(publicDataApiClient.getEventDtoList()).willReturn(eventDtoList);

            // When
            mockMvc.perform(get("/bridge/v1/batch"));

            // Then
            then(publicDataApiClient).should(times(1)).getEventDtoList();
        }
    }

    private EventDto createEventDto(Long contentId, String title, String area) {
        EventDto eventDto = new EventDto();
        eventDto.setContentId(contentId);
        eventDto.setTitle(title);
        eventDto.setArea(area);
        eventDto.setCreatedTime(LocalDateTime.now());
        eventDto.setModifiedTime(LocalDateTime.now());
        return eventDto;
    }

}