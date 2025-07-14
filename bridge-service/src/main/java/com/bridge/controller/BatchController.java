package com.bridge.controller;

import com.bridge.model.dto.EventDto;
import com.bridge.service.PublicDataApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/bridge/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final PublicDataApiClient publicDataApiClient;

    /**
     * 지역기반관광정보조회 API 조회
     *
     * 테스트를 위한 코드입니다.
     *
     * @return List of EventDto
     */
    @GetMapping(path = "/batch")
    public ResponseEntity<List<EventDto>> setEventDtoList() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(publicDataApiClient.getEventDtoList());
    }

}
