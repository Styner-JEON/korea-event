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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/events/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventListResponse>> getEventList(
            @PageableDefault(
                    size = 20,
//                    page = 0,
                    sort = "modifiedTime",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(eventService.selectEventList(pageable, query));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long contentId) {
        return ResponseEntity.ok(eventService.selectEvent(contentId));
    }

}