package com.bridge.controller;

import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(BatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BatchController 단위 테스트")
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PublicDataApiClient publicDataApiClient;

    @Nested
    @DisplayName("GET /bridge/v1/batch - 이벤트 목록 조회")
    class SetEventDtoListTest {
        @Test
        @DisplayName("이벤트 목록 정상 조회 - 2개 반환")
        void givenTwoEvents_whenGetEventList_thenReturnsTwo() throws Exception {
            // Given
            EventDto eventDto = createEventDto("1", "서울 빛초롱 축제", "서울");
            EventDto eventDto2 = createEventDto("2", "부산 불꽃 축제", "부산");
            List<EventDto> eventDtoList = List.of(eventDto, eventDto2);

            given(publicDataApiClient.getEventDtoList()).willReturn(eventDtoList);

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].contentId").value("1"))
                    .andExpect(jsonPath("$[0].title").value("서울 빛초롱 축제"))
                    .andExpect(jsonPath("$[1].contentId").value("2"))
                    .andExpect(jsonPath("$[1].title").value("부산 불꽃 축제"));

            then(publicDataApiClient).should().getEventDtoList();
        }

        @Test
        @DisplayName("이벤트 목록이 비어있을 때 빈 리스트 반환")
        void givenNoEvents_whenGetEventList_thenReturnsEmptyList() throws Exception {
            // Given
            given(publicDataApiClient.getEventDtoList()).willReturn(List.of());

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));

            then(publicDataApiClient).should().getEventDtoList();
        }

        @Test
        @DisplayName("PublicDataApiClient에서 예외 발생 시 500")
        void givenServiceException_whenGetEventList_thenReturns500() throws Exception {
            // Given
            given(publicDataApiClient.getEventDtoList()).willThrow(new RuntimeException("API 호출 실패"));

            // When
            ResultActions resultActions = mockMvc.perform(get("/bridge/v1/batch")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andDo(print())
                    .andExpect(status().isInternalServerError());

            then(publicDataApiClient).should().getEventDtoList();
        }
    }

    private EventDto createEventDto(String contentId, String title, String area) {
        EventDto eventDto = new EventDto();
        eventDto.setContentId(Long.parseLong(contentId));
        eventDto.setTitle(title);
        eventDto.setArea(area);
        eventDto.setAddr1("테스트 주소");
        eventDto.setEventStartDate(LocalDate.now());
        eventDto.setEventEndDate(LocalDate.now().plusDays(7));
        eventDto.setCreatedTime(LocalDateTime.now());
        eventDto.setModifiedTime(LocalDateTime.now());
        return eventDto;
    }

}