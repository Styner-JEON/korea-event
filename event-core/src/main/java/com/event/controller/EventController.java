package com.event.controller;

import com.event.exception.CustomEventException;
import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 이벤트 관련 요청을 처리하는 REST 컨트롤러
 *
 * 이벤트 목록 조회 및 상세 정보 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping(path = "/events/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    /**
     * 이벤트 목록을 페이지네이션과 함께 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @param query    검색 쿼리 (선택사항)
     * @param area     지역 필터 (선택사항)
     * @return 이벤트 목록 페이지
     */
    @GetMapping
    public ResponseEntity<Page<EventListResponse>> getEventList(
            @PageableDefault(size = 20,
                    // page = 0,
                    sort = "modifiedTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String area) {
        return ResponseEntity.ok(eventService.selectEventList(pageable, query, area));
    }

    /**
     * 특정 이벤트의 상세 정보를 조회합니다.
     *
     * @param contentId 이벤트 컨텐츠 ID
     * @return 이벤트 상세 정보
     */
    @GetMapping("/{contentId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long contentId) {
        return ResponseEntity.ok(eventService.selectEvent(contentId));
    }

}