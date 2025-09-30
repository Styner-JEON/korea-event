package com.event.controller;

import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.security.JwtAuthenticationFilter;
import com.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EventController 단위 테스트")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("getEventList")
    class GetEventListTest {
        @Test
        @DisplayName("이벤트 리스트 정상 조회 - 2개 반환")
        void givenTwoEvents_whenGetEventList_thenReturnsTwo() throws Exception {
            // Given
            EventListResponse eventListResponse = new EventListResponse(1L, "축제1", "서울", "img1.jpg", LocalDate.now(), LocalDate.now().plusDays(1));
            EventListResponse eventListResponse2 = new EventListResponse(2L, "축제2", "부산", "img2.jpg", LocalDate.now(), LocalDate.now().plusDays(2));
            Pageable pageable = PageRequest.of(0, 20);
            Page<EventListResponse> eventListResponsePage = new PageImpl<>(List.of(eventListResponse, eventListResponse2), pageable, 2);

            given(eventService.selectEventList(any(), eq(null), eq(null))).willReturn(eventListResponsePage);

            // When
            ResultActions result = mockMvc.perform(get("/events/v1")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].contentId").value(1L))
                    .andExpect(jsonPath("$.content[1].contentId").value(2L));

            then(eventService).should().selectEventList(any(), eq(null), eq(null));
        }
    }

    @Nested
    @DisplayName("getEvent")
    class GetEventTest {
        @Test
        @DisplayName("이벤트 상세 조회 성공")
        void givenValidContentId_whenGetEvent_thenReturnsEventDetail() throws Exception {
            // Given
            EventResponse eventResponse = new EventResponse(
                    1L,
                    "서울 불꽃 축제",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now(),
                    "서울시 영등포구",
                    "",
                    "서울",
                    "img.jpg",
                    "img2.jpg",
                    126.97,
                    37.56,
                    "12345",
                    "http://homepage.com",
                    "이벤트 설명",
                    LocalDate.now(),
                    LocalDate.now().plusDays(2),
                    "18:00 ~ 21:00",
                    "10:00 ~ 22:00",
                    "서울시",
                    "02-0000-0000",
                    "부산시",
                    "051-0000-0000",
                    Instant.now()
            );

            given(eventService.selectEvent(1L)).willReturn(eventResponse);

            // When
            ResultActions result = mockMvc.perform(get("/events/v1/1")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("서울 불꽃 축제"))
                    .andExpect(jsonPath("$.area").value("서울"));

            then(eventService).should().selectEvent(1L);
        }
    }

}
