package com.bridge.logging;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * RequestId 설정
 *
 * 배치 실행 단위로 ULID 생성
 * MDC에 requestId 저장
 * 모든 로그에 자동 포함됨
 */
@Component
@Slf4j
public class BatchRequestIdContext {

    public static final String MDC_KEY = "requestId";

    @PostConstruct
    public void init() {
        String requestId = UlidCreator.getUlid().toString();
        MDC.put(MDC_KEY, requestId);
        log.info("Bridge batch started. requestId={}", requestId);
    }

    @PreDestroy
    public void clear() {
        log.info("Bridge batch finished.");
        MDC.remove(MDC_KEY);
    }

}
