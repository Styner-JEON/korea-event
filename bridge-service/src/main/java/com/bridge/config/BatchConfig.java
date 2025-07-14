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

        /**
         * 공공데이터 API들로부터 이벤트 정보를 페칭하고 카프카로 전송하는 배치 Job
         *
         * 세 개의 스텝을 순차적으로 실행하며, 각 Step이 완료되면 다음 스텝으로 진행합니다.
         * 
         * @param jobRepository             Spring Batch 작업 저장소
         * @param areaBasedListFetchingStep 1단계: 지역 기반 이벤트 리스트를 조회하는 Step
         * @param detailFetchingStep        2단계: 각 이벤트의 상세 정보를 조회하는 Step
         * @param sendingToKafkaStep        3단계: 종합한 이벤트 데이터를 카프카로 전송하는 Step
         * @return 구성된 배치 작업 객체
         */
        @Bean
        public Job eventFetchingAndSendingJob(
                        JobRepository jobRepository,
                        Step areaBasedListFetchingStep,
                        Step detailFetchingStep,
                        Step sendingToKafkaStep) {
                return new JobBuilder("eventFetchingAndSendingJob", jobRepository)
                                .incrementer(new RunIdIncrementer())  // 각 실행마다 고유한 ID 생성
                                .start(areaBasedListFetchingStep)     // 첫번째 스텝
                                .next(detailFetchingStep)             // 두번째 스텝
                                .next(sendingToKafkaStep)             // 세번째 스텝
                                .build();
        }

        /**
         * [1단계 스텝] 지역 기반 이벤트 리스트를 조회하는 Step
         * 
         * 지역기반관광정보조회 API를 호출하여 지역 기반 이벤트 리스트를 조회하는 스텝입니다.
         * 이 단계에서는 기본적인 이벤트 정보(ID, 제목, 주소 등)를 조회합니다.
         * 
         * @param jobRepository      Spring Batch 작업 저장소
         * @param transactionManager 트랜잭션 매니저 - Step 실행 중 트랜잭션 관리
         * @param tasklet            지역 기반 리스트 조회 로직을 담당하는 Tasklet
         * @return 구성된 Step 객체
         */
        @Bean
        public Step areaBasedListFetchingStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        AreaBasedListFetchingTasklet tasklet) {
                return new StepBuilder("areaBasedListFetchingStep", jobRepository)
                                .tasklet(tasklet, transactionManager)
                                .build();
        }

        /**
         * [2단계 스텝] 각 이벤트의 상세 정보를 조회하는 Step
         *
         * 첫번째 스텝에서 가져온 이벤트 목록을 기반으로
         * 공통정보조회 API와 소개정보조회 API를 호출해서
         * 각 이벤트의 상세 정보를 조회하는 스텝입니다.
         *
         * 이 단계에서는 이벤트의 상세 설명, 이미지, 위치 정보 등을 추가로 가져옵니다.
         * 
         * @param jobRepository      Spring Batch 작업 저장소
         * @param transactionManager 트랜잭션 매니저 - 스텝 실행 중 트랜잭션 관리
         * @param tasklet            상세 정보 조회 로직을 담당하는 Tasklet
         * @return 구성된 스텝 객체
         */
        @Bean
        public Step detailFetchingStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        DetailFetchingTasklet tasklet) {
                return new StepBuilder("detailFetchingStep", jobRepository)
                                .tasklet(tasklet, transactionManager)
                                .build();
        }

        /**
         * [3단계 스텝] 카프카로 데이터를 전송하는 Step
         * 
         * 이전 스텝들로부터 수집한 완전한 이벤트 데이터를 카프카로 전송하는 스텝입니다.
         * 
         * @param jobRepository      Spring Batch 작업 저장소
         * @param transactionManager 트랜잭션 매니저 - 스텝 실행 중 트랜잭션 관리
         * @param tasklet            카프카 전송 로직을 담당하는 Tasklet
         * @return 구성된 스텝 객체
         */
        @Bean
        public Step sendingToKafkaStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        SendingToKafkaTasklet tasklet) {
                return new StepBuilder("sendingToKafkaStep", jobRepository)
                                .tasklet(tasklet, transactionManager)
                                .build();
        }

}
