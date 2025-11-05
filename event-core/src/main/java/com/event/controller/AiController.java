package com.event.controller;

import com.event.model.response.CommentAnalysisResponse;
import com.event.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 기반 기능을 제공하는 REST 컨트롤러
 *
 * 댓글 분석 및 요약 기능을 제공합니다.
 */
@RestController
@RequestMapping(path = "/ai/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    /**
     * 특정 이벤트의 댓글들을 AI로 분석합니다.
     * 
     * @param contentId 이벤트 컨텐츠 ID
     * @return 댓글 분석 결과
     */
    @GetMapping("/{contentId}/analysis")
    @Operation(summary = "댓글 AI 분석 조회")
    ResponseEntity<CommentAnalysisResponse> getCommentsAnalysis(@PathVariable Long contentId) {
        return ResponseEntity.ok(aiService.analyzeComments(contentId));
    }

}
