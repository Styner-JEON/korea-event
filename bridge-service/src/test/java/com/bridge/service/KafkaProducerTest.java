package com.bridge.service;

import com.bridge.exception.CustomKafkaException;
import com.bridge.model.dto.EventDto;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducer 단위 테스트")
class KafkaProducerTest {

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @Mock
    private KafkaTemplate<String, EventDto> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, EventDto>> completableFuture;

    @Mock
    private SendResult<String, EventDto> sendResult;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducer, "topic", "test-topic");
        ReflectionTestUtils.setField(kafkaProducer, "timeoutSeconds", 5L);
    }

    @Nested
    @DisplayName("sendEventDto")
    class SendEventDtoTest {
        @Test
        @DisplayName("카프카 메시지 전송 성공")
        void givenValidEventDto_whenSendEventDto_thenSendsSuccessfully() throws Exception {
            // Given
            EventDto eventDto = createEventDto();
            RecordMetadata mockMetadata = mock(RecordMetadata.class);

            given(sendResult.getRecordMetadata()).willReturn(mockMetadata);
            given(kafkaTemplate.send(eq("test-topic"), eq(eventDto))).willReturn(completableFuture);
            given(completableFuture.get(5L, TimeUnit.SECONDS)).willReturn(sendResult);

            // When & Then
            assertThatCode(() -> kafkaProducer.sendEventDto(eventDto))
                    .doesNotThrowAnyException();

            then(kafkaTemplate).should().send("test-topic", eventDto);
        }

        @Test
        @DisplayName("카프카 메시지 전송 실패 - TimeoutException")
        void givenTimeoutException_whenSendEventDto_thenThrowsCustomKafkaException() throws Exception {
            // Given
            EventDto eventDto = createEventDto();

            given(kafkaTemplate.send(eq("test-topic"), eq(eventDto))).willReturn(completableFuture);
            given(completableFuture.get(5L, TimeUnit.SECONDS)).willThrow(new TimeoutException("Timeout occurred"));

            // When & Then
            assertThatThrownBy(() -> kafkaProducer.sendEventDto(eventDto))
                    .isInstanceOf(CustomKafkaException.class);

            then(kafkaTemplate).should().send("test-topic", eventDto);
        }

        @Test
        @DisplayName("카프카 메시지 전송 실패 - ExecutionException")
        void givenExecutionException_whenSendEventDto_thenThrowsCustomKafkaException() throws Exception {
            // Given
            EventDto eventDto = createEventDto();

            given(kafkaTemplate.send(eq("test-topic"), eq(eventDto))).willReturn(completableFuture);
            given(completableFuture.get(5L, TimeUnit.SECONDS))
                    .willThrow(new ExecutionException("Execution failed", new RuntimeException()));

            // When & Then
            assertThatThrownBy(() -> kafkaProducer.sendEventDto(eventDto))
                    .isInstanceOf(CustomKafkaException.class);

            then(kafkaTemplate).should().send("test-topic", eventDto);
        }

        @Test
        @DisplayName("카프카 메시지 전송 실패 - InterruptedException")
        void givenInterruptedException_whenSendEventDto_thenThrowsCustomKafkaException() throws Exception {
            // Given
            EventDto eventDto = createEventDto();

            given(kafkaTemplate.send(eq("test-topic"), eq(eventDto))).willReturn(completableFuture);
            given(completableFuture.get(5L, TimeUnit.SECONDS)).willThrow(new InterruptedException("Thread interrupted"));

            // When & Then
            assertThatThrownBy(() -> kafkaProducer.sendEventDto(eventDto))
                    .isInstanceOf(CustomKafkaException.class);

            then(kafkaTemplate).should().send("test-topic", eventDto);
        }
    }

    private EventDto createEventDto() {
        EventDto eventDto = new EventDto();
        eventDto.setContentId(1L);
        eventDto.setTitle("테스트 축제");
        eventDto.setCreatedTime(LocalDateTime.now());
        eventDto.setModifiedTime(LocalDateTime.now());
        eventDto.setAddr1("서울특별시 강남구");
        eventDto.setAddr2("테헤란로");
        eventDto.setArea("서울");
        eventDto.setFirstImage("http://example.com/image1.jpg");
        eventDto.setFirstImage2("http://example.com/image2.jpg");
        eventDto.setMapX(127.0276);
        eventDto.setMapY(37.4979);
        eventDto.setZipCode("06292");
        eventDto.setHomepage("http://example.com");
        eventDto.setOverview("축제 개요");
        eventDto.setEventStartDate(LocalDate.now());
        eventDto.setEventEndDate(LocalDate.now().plusDays(7));
        eventDto.setPlayTime("10:00~18:00");
        eventDto.setUseTimeFestival("매일 운영");
        eventDto.setSponsor1("주최기관");
        eventDto.setSponsor1Tel("02-1234-5678");
        eventDto.setSponsor2("후원기관");
        eventDto.setSponsor2Tel("02-8765-4321");
        return eventDto;
    }

}
