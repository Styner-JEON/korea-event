package com.bridge.tasklet;

import com.bridge.model.dto.EventDto;
import com.bridge.service.KafkaProducer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 카프카 이벤트 데이터 전송 TaskletStep
 *
 * 이전 스텝들을 통해 생성한 완전한 이벤트 데이터를 카프카 토픽으로 전송하는 역할을 담당합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SendingToKafkaTasklet implements Tasklet {

    private final KafkaProducer kafkaProducer;

    private final RetryTemplate retryTemplate;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // JobExecutionContext에서 완성된 이벤트 리스트 조회
        List<EventDto> eventDtoList = getEventDtoList(contribution);
        // 각 이벤트에 대해 카프카 전송 처리
        eventDtoList.forEach(this::sendWithRetry);

        return RepeatStatus.FINISHED;
    }

    private List<EventDto> getEventDtoList(StepContribution stepContribution) {
        JobExecution jobExecution = stepContribution.getStepExecution().getJobExecution();
        ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
        // 완성된 이벤트 리스트 조회
        Object eventDtoListObj = jobExecutionContext.get("eventDtoList");

        // EventDto 리스트로 변환
        if (eventDtoListObj instanceof List<?>) {
            return ((List<?>) eventDtoListObj).stream()
                    .filter(EventDto.class::isInstance)
                    .map(EventDto.class::cast)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private void sendWithRetry(EventDto eventDto) {
        // 카프카 전송에 재시도 로직 적용
        retryTemplate.execute(retryContext -> {
            log.info("Sending eventDto for contentId={} (Retry: {})", eventDto.getContentId(), retryContext.getRetryCount());

            // 카프카 프로듀서를 통해 EventDto 전송
            kafkaProducer.sendEventDto(eventDto);
            return null;
        });
    }

}
