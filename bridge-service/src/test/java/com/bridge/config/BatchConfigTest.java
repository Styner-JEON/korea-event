package com.bridge.config;

import com.bridge.tasklet.AreaBasedListFetchingTasklet;
import com.bridge.tasklet.DetailFetchingTasklet;
import com.bridge.tasklet.SendingToKafkaTasklet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * BatchConfig 클래스의 단위 테스트
 * <p>
 * Spring Batch Job과 Step들이 올바르게 구성되는지 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchConfig 테스트")
class BatchConfigTest {

    @InjectMocks
    private BatchConfig batchConfig;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private AreaBasedListFetchingTasklet areaBasedListFetchingTasklet;

    @Mock
    private DetailFetchingTasklet detailFetchingTasklet;

    @Mock
    private SendingToKafkaTasklet sendingToKafkaTasklet;

    @Test
    @DisplayName("eventFetchingAndSendingJob 빈이 올바르게 생성되는지 테스트")
    void testEventFetchingAndSendingJobCreation() {
        // Given
        Step mockAreaBasedStep = mock(Step.class);
        Step mockDetailStep = mock(Step.class);
        Step mockKafkaStep = mock(Step.class);

        // When
        Job job = batchConfig.eventFetchingAndSendingJob(
                jobRepository,
                mockAreaBasedStep,
                mockDetailStep,
                mockKafkaStep
        );

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("eventFetchingAndSendingJob");
    }

    @Test
    @DisplayName("areaBasedListFetchingStep 빈이 올바르게 생성되는지 테스트")
    void testAreaBasedListFetchingStepCreation() {
        // Given

        // When
        Step step = batchConfig.areaBasedListFetchingStep(
                jobRepository,
                transactionManager,
                areaBasedListFetchingTasklet
        );

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("areaBasedListFetchingStep");
    }

    @Test
    @DisplayName("detailFetchingStep 빈이 올바르게 생성되는지 테스트")
    void testDetailFetchingStepCreation() {
        // Given

        // When
        Step step = batchConfig.detailFetchingStep(
                jobRepository,
                transactionManager,
                detailFetchingTasklet);

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("detailFetchingStep");
    }

    @Test
    @DisplayName("sendingToKafkaStep 빈이 올바르게 생성되는지 테스트")
    void testSendingToKafkaStepCreation() {
        // When
        Step step = batchConfig.sendingToKafkaStep(
                jobRepository,
                transactionManager,
                sendingToKafkaTasklet);

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("sendingToKafkaStep");
    }

    @Test
    @DisplayName("모든 Step들이 null이 아닌 객체로 생성되는지 테스트")
    void testAllStepsAreNotNull() {
        // When
        Step areaBasedStep = batchConfig.areaBasedListFetchingStep(
                jobRepository, transactionManager, areaBasedListFetchingTasklet);
        Step detailStep = batchConfig.detailFetchingStep(
                jobRepository, transactionManager, detailFetchingTasklet);
        Step kafkaStep = batchConfig.sendingToKafkaStep(
                jobRepository, transactionManager, sendingToKafkaTasklet);

        // Then
        assertThat(areaBasedStep).isNotNull();
        assertThat(detailStep).isNotNull();
        assertThat(kafkaStep).isNotNull();
    }

}