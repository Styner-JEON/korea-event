package com.event.controller;

import com.event.model.response.EventFavoriteResponse;
import com.event.security.CustomPrincipal;
import com.event.service.EventFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/events/${api.version}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class EventFavoriteController {

    private final EventFavoriteService eventFavoriteService;

    /**
     * 특정 이벤트를 즐겨찾기합니다.
     * @param contentId
     * @param customPrincipal
     * @return
     */
    @PostMapping("/{contentId}/favorite")
    @Operation(summary = "이벤트 즐겨찾기 추가")
    public ResponseEntity<EventFavoriteResponse> addFavorite(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(eventFavoriteService.addFavorite(contentId, customPrincipal.userId()));
    }

    /**
     * 특정 이벤트의 즐겨찾기를 해제합니다.
     * @param contentId
     * @param customPrincipal
     * @return
     */
    @DeleteMapping("/{contentId}/favorite")
    @Operation(summary = "이벤트 즐겨찾기 해제")
    public ResponseEntity<EventFavoriteResponse> removeFavorite(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        return ResponseEntity.ok(eventFavoriteService.removeFavorite(contentId, customPrincipal.userId()));
    }

}
