package com.event.integration;

import com.event.model.entity.EventEntity;
import com.event.repository.EventRepository;
import com.event.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("이벤트 통합 테스트")
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwt;

    private static final Long TEST_USER_ID = 100L;

    private static final String TEST_USERNAME = "testuser";

    private static final String TEST_USER_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() throws Exception {
        jwt = generateJwt();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Nested
    @DisplayName("이벤트 목록 조회")
    class GetEventListTest {
        @Test
        @DisplayName("이벤트 여러 개가 존재할 때, 이벤트 목록이 정상 조회된다")
        void givenMultipleEvents_whenGetEventList_thenReturnsPage() throws Exception {
            // Given
            eventRepository.save(createEventEntity(1L, "서울 꽃축제", "서울"));
            eventRepository.save(createEventEntity(2L, "부산 불꽃축제", "부산"));

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[*].title").value(
                            containsInAnyOrder("서울 꽃축제", "부산 불꽃축제")));
        }

        @Test
        @DisplayName("검색어 + 지역이 주어졌을 때 필터링된 이벤트 목록이 반환된다")
        void givenQueryAndArea_whenGetEventList_thenReturnsFilteredPage() throws Exception {
            // Given
            eventRepository.save(createEventEntity(1L, "서울 전통시장축제", "서울")); // 포함
            eventRepository.save(createEventEntity(2L, "부산 해양축제", "부산"));    // 포함
            eventRepository.save(createEventEntity(3L, "대전 미술전시", "대전"));

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1")
                    .param("query", "축제")
                    .param("area", "서울,부산")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[*].area").value(
                            containsInAnyOrder("서울", "부산")));
        }
    }

    @Nested
    @DisplayName("이벤트 상세 조회")
    class GetEventDetailTest {
        @Test
        @DisplayName("존재하는 이벤트 ID로 상세 조회 시 200과 데이터 반환")
        void givenValidContentId_whenGetEvent_thenReturns200AndDetail() throws Exception {
            // Given
            EventEntity eventEntity = eventRepository.save(createEventEntity(10L, "한강 페스티벌", "서울"));

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/" + eventEntity.getContentId())
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contentId").value(10))
                    .andExpect(jsonPath("$.title").value("한강 페스티벌"))
                    .andExpect(jsonPath("$.area").value("서울"));
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 ID로 조회 시 404 반환")
        void givenInvalidContentId_whenGetEvent_thenReturns404() throws Exception {
            // Given

            // When
            ResultActions resultActions = mockMvc.perform(get("/events/v1/9999")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            resultActions
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 테스트용 JWT를 직접 생성하는 메서드.
     * <p>
     * Spring Security 환경에서 JwtAuthenticationFilter를 통과한 것처럼 테스트하기 위해,
     * 실제 `JwtUtil` 내부에 정의된 `secretKey` 필드를 리플렉션을 통해 접근하여 서명 키를 추출하고
     * 해당 키로 서명된 JWT 토큰을 생성한다.
     * </p>
     *
     * @return 테스트용 유효한 JWT 문자열
     * @throws Exception 리플렉션 접근 또는 키 추출 과정에서 발생할 수 있는 예외
     */
    private String generateJwt() throws Exception {
        Field secretKeyField = jwtUtil.getClass().getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        SecretKey secretKey = (SecretKey) secretKeyField.get(jwtUtil);

        long now = System.currentTimeMillis();
        long expiration = 1000 * 60 * 10;

        return Jwts.builder()
                .subject(String.valueOf(TEST_USER_ID))
                .claim("username", TEST_USERNAME)
                .claim("userRole", TEST_USER_ROLE)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(secretKey)
                .compact();
    }

    private EventEntity createEventEntity(Long contentId, String title, String area) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setContentId(contentId);
        eventEntity.setTitle(title);
        eventEntity.setArea(area);
        eventEntity.setCreatedTime(LocalDateTime.now().minusDays(1));
        eventEntity.setModifiedTime(LocalDateTime.now());
        eventEntity.setEventStartDate(LocalDate.now());
        eventEntity.setEventEndDate(LocalDate.now().plusDays(1));
        eventEntity.setDbUpdatedAt(LocalDateTime.now());
        return eventEntity;
    }

}
