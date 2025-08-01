package com.bridge.tasklet;

import com.bridge.model.dto.EventDto;
import com.bridge.service.KafkaProducer;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendingToKafkaTasklet 단위 테스트")
class SendingToKafkaTaskletTest {

    @InjectMocks
    private SendingToKafkaTasklet sendingToKafkaTasklet;

    @Mock
    private KafkaProducer kafkaProducer;

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
        @DisplayName("이벤트 리스트를 카프카로 정상적으로 전송")
        void givenEventDtoList_whenExecute_thenSendsEachEventToKafka() throws Exception {
            // Given
            List<EventDto> eventDtoList = createEventDtoList();

            given(stepContribution.getStepExecution()).willReturn(stepExecution);
            given(stepExecution.getJobExecution()).willReturn(jobExecution);
            given(jobExecution.getExecutionContext()).willReturn(executionContext);
            given(executionContext.get("eventDtoList")).willReturn(eventDtoList);
            given(retryTemplate.execute(any())).willReturn(null);

            // When
            RepeatStatus repeatStatus = sendingToKafkaTasklet.execute(stepContribution, chunkContext);

            // Then
            assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);

            // 각 이벤트에 대해 카프카 전송이 호출되었는지 검증
            then(retryTemplate).should(times(eventDtoList.size())).execute(any());
        }

        @Test
        @DisplayName("리스트에 EventDto가 아닌 객체가 포함되어 있을 때 필터링 처리")
        void givenMixedTypeList_whenExecute_thenFiltersOnlyEventDto() throws Exception {
            // Given
            List<Object> mixedList = new ArrayList<>();
            mixedList.add(createEventDto(1L, "이벤트1"));
            mixedList.add("잘못된 객체");
            mixedList.add(createEventDto(2L, "이벤트2"));
            mixedList.add(123);

            given(stepContribution.getStepExecution()).willReturn(stepExecution);
            given(stepExecution.getJobExecution()).willReturn(jobExecution);
            given(jobExecution.getExecutionContext()).willReturn(executionContext);
            given(executionContext.get("eventDtoList")).willReturn(mixedList);
            given(retryTemplate.execute(any())).willReturn(null);

            // When
            RepeatStatus repeatStatus = sendingToKafkaTasklet.execute(stepContribution, chunkContext);

            // Then
            assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);

            // EventDto 객체 2개에 대해서만 호출되어야 함
            then(retryTemplate).should(times(2)).execute(any());
        }
    }

    private List<EventDto> createEventDtoList() {
        List<EventDto> eventDtoList = new ArrayList<>();
        eventDtoList.add(createEventDto(1L, "서울 축제"));
        eventDtoList.add(createEventDto(2L, "부산 이벤트"));
        return eventDtoList;
    }

    private EventDto createEventDto(Long contentId, String title) {
        EventDto eventDto = new EventDto();
        eventDto.setContentId(contentId);
        eventDto.setTitle(title);
        eventDto.setCreatedTime(LocalDateTime.now());
        eventDto.setModifiedTime(LocalDateTime.now());
        eventDto.setAddr1("서울특별시");
        eventDto.setArea("서울");
        eventDto.setFirstImage("http://example.com/image1.jpg");
        eventDto.setMapX(127.0);
        eventDto.setMapY(37.5);
        eventDto.setEventStartDate(LocalDate.now());
        eventDto.setEventEndDate(LocalDate.now().plusDays(7));
        eventDto.setOverview("이벤트 개요");
        eventDto.setHomepage("http://example.com");
        return eventDto;
    }

}