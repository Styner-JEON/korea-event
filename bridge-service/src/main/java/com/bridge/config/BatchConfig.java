package com.bridge.config;

import com.bridge.tasklet.AreaBasedListFetchingTasklet;
import com.bridge.tasklet.DetailFetchingTasklet;
import com.bridge.tasklet.SendingToKafkaTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class BatchConfig {

    @Bean
    public Job eventFetchingAndSendingJob(
            JobRepository jobRepository,
            Step areaBasedListFetchingStep,
            Step detailFetchingStep,
            Step sendingToKafkaStep
    ) {
        return new JobBuilder("eventFetchingAndSendingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(areaBasedListFetchingStep)
                .next(detailFetchingStep)
                .next(sendingToKafkaStep)
                .build();
    }

    @Bean
    public Step areaBasedListFetchingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AreaBasedListFetchingTasklet tasklet
    ) {
        return new StepBuilder("areaBasedListFetchingStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step detailFetchingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DetailFetchingTasklet tasklet
    ) {
        return new StepBuilder("detailFetchingStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step sendingToKafkaStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SendingToKafkaTasklet tasklet
    ) {
        return new StepBuilder("sendingToKafkaStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

}
