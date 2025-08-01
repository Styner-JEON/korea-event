package com.event.model.response;

import java.util.List;
import java.util.Map;

public record CommentAnalysisResponse(
        String summary,        // 요약 문장
        List<String> keywords, // 키워드 리스트
        Emotion emotion        // 감정 분석 결과
) {
    public record Emotion(
            String overall,            // 전체적인 감정 (예: "positive", "negative", "neutral")
            Map<String, Double> ratio, // 감정별 비율 (예: {"positive":0.7,"negative":0.2,"neutral":0.1})
            List<String> mainEmotions  // 주요 감정(표현) 리스트 (예: ["즐거움", "아쉬움"])
    ) {}
}