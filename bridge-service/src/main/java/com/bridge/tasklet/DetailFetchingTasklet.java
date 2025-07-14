package com.bridge.tasklet;

import com.bridge.exception.CustomBatchException;
import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 이벤트 상세 정보 조회 TaskletStep
 *
 * 첫번째 스텝에서 수집한 기본 이벤트 목록을 기반으로 공통정보조회 API와 소개정보조회 API를 호출합니다.
 * 각 이벤트의 상세 정보를 조회하는 역할을 담당합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DetailFetchingTasklet implements Tasklet {

    private final PublicDataApiClient publicDataApiClient;

    private final RetryTemplate retryTemplate;

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) {
        // 이전 스텝에서 수집한 이벤트 리스트를 EventDto 리스트로 반환
        List<EventDto> eventDtoList = getEventDtoList(stepContribution);
        // 각 이벤트에 대해 공통정보조회 API 호출 및 저장 + 소개정보조회 API 호출 및 저장
        eventDtoList.forEach(this::setEventDto);

        return RepeatStatus.FINISHED;
    }

    /**
     * 첫번째 스텝에서 JobExecutionContext에 저장한 이벤트 리스트를 조회하고 타입 안전성을 보장하여 반환합니다.
     *
     * @param stepContribution 스텝 실행 정보 (JobExecution 접근을 위해 사용)
     * @return 타입 안전성이 보장된 EventDto 리스트 (데이터가 없으면 빈 리스트)
     */
    private List<EventDto> getEventDtoList(StepContribution stepContribution) {
        JobExecution jobExecution = stepContribution.getStepExecution().getJobExecution();
        ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
        // "eventDtoList" 키로 저장된 첫 번째 스텝에서 저장한 데이터를 가져옴
        Object eventDtoListObj = jobExecutionContext.get("eventDtoList");

        // EventDto 리스트로 변환
        if (eventDtoListObj instanceof List<?>) {
            return ((List<?>) eventDtoListObj).stream()
                    .filter(EventDto.class::isInstance)
                    .map(EventDto.class::cast)
                    .collect(Collectors.toList());
        } else {
            // 데이터가 없거나 타입이 맞지 않는 경우 빈 리스트 반환
            return Collections.emptyList();
        }
    }

    /**
     * 주어진 EventDto 객체에 대해 공통정보조회 API와 소개정보조회 API를 호출하여 상세 정보를 추가합니다.
     *
     * @param eventDto 상세 정보를 추가할 이벤트 DTO 객체 (참조로 전달되어 직접 수정됨)
     */
    private void setEventDto(EventDto eventDto) {
        // API 호출에 리트라이 로직 적용
        retryTemplate.execute(retryContext -> {
            Long contentId = eventDto.getContentId();
            log.info("Fetching details for contentId={} (Retry: {})", contentId, retryContext.getRetryCount());

            // 공통정보조회 API 호출해서 공통 상세 정보를 eventDto에 추가
            publicDataApiClient.setDetailCommon(String.valueOf(contentId), eventDto);

            // 소개정보조회 API 호출해서 소개 정보를 eventDto에 추가
            publicDataApiClient.setDetailIntro(String.valueOf(contentId), eventDto);

            return null;
        });
    }

}