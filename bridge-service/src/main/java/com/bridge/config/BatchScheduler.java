package com.bridge.config;

import com.bridge.exception.CustomBatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 배치 스케줄러 설정 클래스
 *
 * 주기적으로 이벤트 데이터 수집 및 카프카 전송 배치 작업을 실행합니다.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    private final Job eventFetchingAndSendingJob;

    /**
     * 테스트를 위한 배칭 스케쥴러
     * 
     * @throws CustomBatchException 배치 작업 실행 실패 시 발생하는 커스텀 예외
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleEventFetchingAndSendingJob() {
        ZonedDateTime nowKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String formattedTime = nowKST.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 매번 다른 시간값으로 새로운 JobInstance를 생성
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("koreanTime", formattedTime)
                .toJobParameters();

        try {
            // 배치 작업 실행
            jobLauncher.run(eventFetchingAndSendingJob, jobParameters);

        } catch (
                JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Batch job execution failed: {}", e.getMessage(), e);
            throw new CustomBatchException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Batch job execution failed: " + e.getMessage());
        }
    }

}
