package com.bridge.tasklet;

import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AreaBasedListFetchingTasklet 단위 테스트")
class AreaBasedListFetchingTaskletTest {

    @InjectMocks
    private AreaBasedListFetchingTasklet areaBasedListFetchingTasklet;

    @Mock
    private PublicDataApiClient publicDataApiClient;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private ExecutionContext executionContext;

    @Nested
    @DisplayName("execute")
    class ExecuteTest {
        @Test
        @DisplayName("정상적인 이벤트 리스트 조회 및 JobExecutionContext 저장")
        void givenValidApiResponse_whenExecute_thenReturnsFinishedAndStoresEventList() throws Exception {
            // Given
            given(stepContribution.getStepExecution()).willReturn(stepExecution);
            given(stepExecution.getJobExecution()).willReturn(jobExecution);
            given(jobExecution.getExecutionContext()).willReturn(executionContext);
            given(retryTemplate.execute(any())).willReturn(createEventDtoList());

            // When
            RepeatStatus repeatStatus = areaBasedListFetchingTasklet.execute(stepContribution, chunkContext);

            // Then
            assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);

            then(retryTemplate).should().execute(any());
            then(executionContext).should().put(eq("eventDtoList"), any(List.class));
        }

        @Test
        @DisplayName("RetryTemplate 호출 검증")
        void givenRetryTemplate_whenExecute_thenCallsRetryTemplate() throws Exception {
            // Given
            given(stepContribution.getStepExecution()).willReturn(stepExecution);
            given(stepExecution.getJobExecution()).willReturn(jobExecution);
            given(jobExecution.getExecutionContext()).willReturn(executionContext);
            given(retryTemplate.execute(any())).willReturn(new ArrayList<>());

            // When
            RepeatStatus repeatStatus = areaBasedListFetchingTasklet.execute(stepContribution, chunkContext);

            // Then
            assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);

            then(retryTemplate).should().execute(any());
        }
    }

    private List<EventDto> createEventDtoList() {
        List<EventDto> eventDtoList = new ArrayList<>();
        eventDtoList.add(createEventDto(1L, "서울 축제", "서울"));
        eventDtoList.add(createEventDto(2L, "부산 이벤트", "부산"));
        return eventDtoList;
    }

    private EventDto createEventDto(Long contentId, String title, String area) {
        EventDto eventDto = new EventDto();
        eventDto.setContentId(contentId);
        eventDto.setTitle(title);
        eventDto.setCreatedTime(LocalDateTime.now());
        eventDto.setModifiedTime(LocalDateTime.now());
        eventDto.setAddr1("");
        eventDto.setArea(area);
        eventDto.setFirstImage("http://example.com/image1.jpg");
        eventDto.setMapX(127.0);
        eventDto.setMapY(37.5);
        return eventDto;
    }

}