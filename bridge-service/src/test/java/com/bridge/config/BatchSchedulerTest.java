package com.bridge.config;

import com.bridge.exception.CustomBatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * BatchScheduler 클래스의 단위 테스트
 * <p>
 * 스케줄링된 배치 작업이 올바르게 실행되고 예외 처리되는지 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchScheduler 테스트")
class BatchSchedulerTest {

    @InjectMocks
    private BatchScheduler batchScheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job eventFetchingAndSendingJob;

    @Mock
    private JobExecution jobExecution;

    @Test
    @DisplayName("스케줄된 배치 작업이 성공적으로 실행되는지 테스트")
    void testScheduleEventFetchingAndSendingJobSuccess() throws Exception {
        // Given
        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // When
        batchScheduler.scheduleEventFetchingAndSendingJob();

        // Then
        verify(jobLauncher, times(1)).run(eq(eventFetchingAndSendingJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("JobParameters에 한국 시간이 포함되는지 테스트")
    void testJobParametersContainsKoreanTime() throws Exception {
        // Given
        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // When
        batchScheduler.scheduleEventFetchingAndSendingJob();

        // Then
        verify(jobLauncher).run(eq(eventFetchingAndSendingJob), argThat(jobParameters -> {
            String koreanTime = jobParameters.getString("koreanTime");
            return koreanTime != null && !koreanTime.isEmpty();
        }));
    }

    @Test
    @DisplayName("JobExecutionAlreadyRunningException 발생 시 CustomBatchException으로 변환되는지 테스트")
    void testJobExecutionAlreadyRunningExceptionHandling() throws Exception {
        // Given
        JobExecutionAlreadyRunningException exception = new JobExecutionAlreadyRunningException("Job is already running");

        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> batchScheduler.scheduleEventFetchingAndSendingJob())
                .isInstanceOf(CustomBatchException.class);

        verify(jobLauncher, times(1)).run(eq(eventFetchingAndSendingJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("JobRestartException 발생 시 CustomBatchException으로 변환되는지 테스트")
    void testJobRestartExceptionHandling() throws Exception {
        // Given
        JobRestartException exception = new JobRestartException("Job restart failed");

        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> batchScheduler.scheduleEventFetchingAndSendingJob())
                .isInstanceOf(CustomBatchException.class);

        verify(jobLauncher, times(1)).run(eq(eventFetchingAndSendingJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("JobInstanceAlreadyCompleteException 발생 시 CustomBatchException으로 변환되는지 테스트")
    void testJobInstanceAlreadyCompleteExceptionHandling() throws Exception {
        // Given
        JobInstanceAlreadyCompleteException exception = new JobInstanceAlreadyCompleteException(
                "Job instance already complete");

        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> batchScheduler.scheduleEventFetchingAndSendingJob())
                .isInstanceOf(CustomBatchException.class);

        verify(jobLauncher, times(1)).run(eq(eventFetchingAndSendingJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("JobParametersInvalidException 발생 시 CustomBatchException으로 변환되는지 테스트")
    void testJobParametersInvalidExceptionHandling() throws Exception {
        // Given
        JobParametersInvalidException exception = new JobParametersInvalidException("Invalid job parameters");

        when(jobLauncher.run(eq(eventFetchingAndSendingJob), any(JobParameters.class)))
                .thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> batchScheduler.scheduleEventFetchingAndSendingJob())
                .isInstanceOf(CustomBatchException.class);

        verify(jobLauncher, times(1)).run(eq(eventFetchingAndSendingJob), any(JobParameters.class));
    }

}