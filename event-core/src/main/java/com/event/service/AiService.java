package com.event.service;

import com.event.exception.CustomAiException;
import com.event.model.entity.CommentEntity;
import com.event.model.response.CommentAnalysisResponse;
import com.event.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 기반 댓글 분석 서비스
 * 
 * 이벤트 댓글을 AI를 통해 분석하여 요약, 키워드 추출, 감정 분석 등을 수행합니다.
 * Spring AI를 사용하여 ChatGPT와 통신하고, 결과를 캐싱하여 성능을 최적화합니다.
 */
@Service
@Slf4j
public class AiService {

    private final ChatClient chatClient;

    private final CommentRepository commentRepository;

    private final ObjectMapper objectMapper;

    @Value("${size.ai-comment}")
    private int requiredCommentCount;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            CommentRepository commentRepository,
            ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.commentRepository = commentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 최소 특정한 개수 이상의 댓글이 있을 때만 분석을 수행하며,
     * 최신 특정한 개수의 댓글을 기준으로 요약, 키워드 추출, 감정 분석을 진행합니다.
     * 결과는 레디스 캐쉬에 저장되어 동일한 요청에 대해 빠른 응답을 제공합니다.
     * 
     * @param contentId 분석할 이벤트의 컨텐츠 ID
     * @return CommentAnalysisResponse 댓글 분석 결과 (요약, 키워드, 감정 분석)
     * @throws CustomAiException 댓글 수가 부족하거나 AI 분석 실패 시 발생
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "comment-analysis", key = "#contentId")
    public CommentAnalysisResponse analyzeComments(Long contentId) {
        // 댓글 수 확인
        int commentCount = commentRepository.countByContentId(contentId);
        if (commentCount < requiredCommentCount) {
            log.warn("Not enough comments found. contentId: {}, required comment count: {}, comment count: {}", contentId, requiredCommentCount, commentCount);
            throw new CustomAiException(HttpStatus.BAD_REQUEST,
                    "Comment analysis is available only when there are " + requiredCommentCount + " or more comments.");
        }

        // 최신 댓글들 조회
        Pageable pageable = PageRequest.of(0, requiredCommentCount);
        List<CommentEntity> commentEntityList = commentRepository.findByContentIdOrderByUpdatedAtDesc(contentId, pageable);

        // 댓글 내용만 추출하여 하나의 문자열로 결합
        String commentListContent = commentEntityList.stream()
                .map(CommentEntity::getContent)
                .collect(Collectors.joining("\n"));

        // 시스템 프롬프트
        String systemText = """
                    당신은 대한민국의 문화 이벤트(축제, 공연, 행사 등) 관련 댓글을 전문적으로 분석하고 요약하는 AI 어시스턴트입니다.

                    ## 수행 항목
                    1. **요약**: 댓글 전체의 분위기와 주요 특징을 3~4문장으로 압축 요약
                    2. **핵심 키워드 추출**: 댓글에서 반복적으로 언급되거나, 주요 이슈/관심사로 보이는 단어(최대 5개) 도출 (쉼표로 구분)
                    3. **감정 분석**: 전체 댓글의 전반적 감정 상태(긍정/부정/중립), 주요 감정 표현 및 그 비중(예: 긍정 70%, 부정 20%, 중립 10%)

                    ## 결과는 아래 JSON 형식으로만 출력하세요:
                    {
                      "summary": "...",
                      "keywords": ["...", "...", "..."],
                      "emotion": {
                        "overall": "...",
                        "ratio": {"positive": 0, "negative": 0, "neutral": 0},
                        "mainEmotions": ["...", "..."]
                      }
                    }
                    기타 분석 및 표현 지침은 기존과 동일.
                    반드시 불필요한 설명, 마크다운 없이 JSON만 응답할 것.
                """;

        // 유저 프롬프트
        String userText = String.format("다음은 %d개의 댓글입니다. 분석해주세요:\n\n%s", commentEntityList.size(), commentListContent);

        // AI 분석 요청 수행
        ChatResponse chatResponse;
        try {
            chatResponse = chatClient.prompt()
                    .system(systemText)
                    .user(userText)
                    .call()
                    .chatResponse();
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage(), e);
            throw new CustomAiException(HttpStatus.BAD_GATEWAY, "AI analysis failed");
        }
        log.debug("AI analysis result: {}", chatResponse);

        // AI 응답 유효성 검증
        if (chatResponse == null ||
                chatResponse.getResult() == null ||
                chatResponse.getResult().getOutput() == null ||
                chatResponse.getResult().getOutput().getText() == null) {
            log.error("AI response is null or empty: {}", chatResponse);
            throw new CustomAiException(HttpStatus.BAD_GATEWAY, "AI service returned a null or empty response.");
        }

        String textContent = chatResponse.getResult().getOutput().getText().trim();
        log.info("Extracted AI response text: {}", textContent);

        // 마크다운 형식의 JSON인 경우에는, 순수 JSON로 변경
        try {
            if (textContent.startsWith("```") || textContent.startsWith("```json")) {
                int start = textContent.indexOf("{");
                int end = textContent.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    textContent = textContent.substring(start, end + 1);
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract JSON from markdown: {}", textContent, e);
            throw new CustomAiException(HttpStatus.BAD_GATEWAY, "Failed to extract JSON from markdown");
        }

        // JSON 파싱하여 응답 객체로 변환
        try {
            return objectMapper.readValue(textContent, CommentAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", textContent, e);
            throw new CustomAiException(HttpStatus.BAD_GATEWAY, "Failed to parse AI response");
        }
    }

}
