package com.bridge.tasklet;

import com.bridge.exception.CustomBatchException;
import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 지역 기반 이벤트 리스트 조회 TaskletStep
 *
 * 지역기반관광정보조회 API를 호출합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AreaBasedListFetchingTasklet implements Tasklet {

    private final PublicDataApiClient publicDataApiClient;

    private final RetryTemplate retryTemplate;

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) {
        // 이벤트 리스트 조회
        List<EventDto> eventDtoList = fetchEventDtoList();

        // 조회 결과를 JobExecutionContext에 저장하여 다음 스텝에서 사용할 수 있도록 함
        stepContribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("eventDtoList", eventDtoList);

        return RepeatStatus.FINISHED;
    }

    private List<EventDto> fetchEventDtoList() {
        // API 호출에 리트라이 로직 적용
        return retryTemplate.execute(retryContext -> {
            log.info("Fetching AreaBasedList (Retry: {})", retryContext.getRetryCount());
            int pageNo = 1;
            List<EventDto> eventDtoList = new ArrayList<>();

            // 한 페이지에 해당하는 데이터들을 페칭한 다음 리스트에 저장하는 것을 반복
            while (publicDataApiClient.setAreaBasedList(pageNo, eventDtoList)) {
                pageNo++;
            }
            return eventDtoList;
        });
    }

}
