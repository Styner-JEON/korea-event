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

@Component
@RequiredArgsConstructor
@Slf4j
public class SendingToKafkaTasklet implements Tasklet {

    private final KafkaProducer kafkaProducer;

    private final RetryTemplate retryTemplate;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, ChunkContext chunkContext) {
        List<EventDto> eventDtoList = getEventDtoList(contribution);
        eventDtoList.forEach(this::sendWithRetry);
        return RepeatStatus.FINISHED;
    }

    private List<EventDto> getEventDtoList(StepContribution stepContribution) {
        JobExecution jobExecution = stepContribution.getStepExecution().getJobExecution();
        ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
        Object eventDtoListObj = jobExecutionContext.get("eventDtoList");
        if (eventDtoListObj instanceof List<?>) {
            return ((List<?>) eventDtoListObj).stream()
                    .filter(EventDto.class::isInstance)
                    .map(EventDto.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void sendWithRetry(EventDto eventDto) {
        retryTemplate.execute(retryContext -> {
            kafkaProducer.sendEventDto(eventDto, retryContext.getRetryCount());
            return null;
        });
    }

}
