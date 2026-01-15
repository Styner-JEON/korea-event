package com.event.controller;

import com.event.model.response.EventListResponse;
import com.event.model.response.EventResponse;
import com.event.security.CustomPrincipal;
import com.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Value("${size.event}")
    private int eventSize;

    @Value("${sort.event.direction}")
    private String eventSortDirection;

    @Value("${sort.event.property}")
    private String eventSortProperty;

    /**
     * 이벤트 목록을 페이지네이션과 함께 조회합니다.
     *
     * @param pageable   페이지네이션 정보
     * @param query      검색 쿼리 (선택사항)
     * @param areaString 지역 필터 (선택사항)
     * @return 이벤트 목록 페이지
     */
    @GetMapping
    @Operation(summary = "이벤트 목록 조회")
    public ResponseEntity<Page<EventListResponse>> getEventList(
            Pageable pageable,
            @RequestParam(required = false) String query,
            @RequestParam(name = "area", required = false) String areaString
    ) {
        Sort.Direction direction = Sort.Direction.fromString(eventSortDirection);
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                eventSize,
                Sort.by(direction, eventSortProperty));
        return ResponseEntity.ok(eventService.selectEventList(fixedPageable, query, areaString));
    }

    /**
     * 특정 이벤트의 상세 정보를 조회합니다.
     *
     * @param contentId 이벤트 컨텐츠 ID
     * @return 이벤트 상세 정보
     */
    @GetMapping("/{contentId}")
    @Operation(summary = "이벤트 상세 조회")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Long userId = (customPrincipal != null) ? customPrincipal.userId() : null;
        return ResponseEntity.ok(eventService.selectEvent(contentId, userId));
    }

    /**
     * 인증된 사용자의 즐겨찾기 이벤트 목록을 조회합니다.
     *
     * @param pageable
     * @param customPrincipal
     * @param query
     * @param areaString
     * @return
     */
    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기 이벤트 목록 조회")
    public ResponseEntity<Page<EventListResponse>> getFavoriteEventList(
            Pageable pageable,
            @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @RequestParam(required = false) String query,
            @RequestParam(name = "areastring", required = false) String areaString) {
        Sort.Direction direction = Sort.Direction.fromString(eventSortDirection);
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                eventSize,
                Sort.by(direction, eventSortProperty));
        Long userId = customPrincipal.userId();
        return ResponseEntity.ok(eventService.selectFavoriteEventList(fixedPageable, userId, query, areaString));
    }

}