package com.event.controller;

import com.event.model.response.CommentAnalysisResponse;
import com.event.model.response.CommentAnalysisResponse.Emotion;
import com.event.security.JwtAuthenticationFilter;
import com.event.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AiController 단위 테스트")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiService aiService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("getCommentsSummary")
    class GetCommentsSummaryTest {
        @Test
        @DisplayName("댓글 분석 결과를 성공적으로 반환한다")
        void givenValidContentId_whenGetSummary_thenReturnsCommentAnalysisResponse() throws Exception {
            // Given
            Long contentId = 1L;
            String summary = "이 이벤트는 즐거움과 감동이 어우러진 축제로 보입니다.";
            CommentAnalysisResponse commentAnalysisResponse = new CommentAnalysisResponse(
                    summary,
                    List.of("불꽃놀이", "음식", "가족", "사진", "사람들"),
                    new Emotion("positive",
                            Map.of("positive", 0.7, "negative", 0.1, "neutral", 0.2),
                            List.of("즐거움", "감동"))
            );

            given(aiService.analyzeComments(contentId)).willReturn(commentAnalysisResponse);

            // When
            ResultActions result = mockMvc.perform(get("/ai/v1/{contentId}/analysis", contentId)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.summary").value(summary))
                    .andExpect(jsonPath("$.keywords.length()").value(5))
                    .andExpect(jsonPath("$.emotion.overall").value("positive"))
                    .andExpect(jsonPath("$.emotion.ratio.positive").value(0.7))
                    .andExpect(jsonPath("$.emotion.ratio.negative").value(0.1))
                    .andExpect(jsonPath("$.emotion.ratio.neutral").value(0.2))
                    .andExpect(jsonPath("$.emotion.mainEmotions.length()").value(2));

            then(aiService).should().analyzeComments(contentId);
        }
    }

}
