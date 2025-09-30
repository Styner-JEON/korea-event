package com.event.controller;

import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.model.response.CommentResponse;
import com.event.model.response.CommentScrollResponse;
import com.event.security.CustomPrincipal;
import com.event.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 관련 요청을 처리하는 REST 컨트롤러
 *
 * 특정 이벤트에 대한 댓글의 조회, 등록, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequestMapping(path = "/events/${api.version}/{contentId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 특정 이벤트의 댓글 수를 조회합니다.
     *
     * @param contentId 이벤트 컨텐츠 ID
     * @return 댓글 수
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getCommentCount(@PathVariable Long contentId) {
        return ResponseEntity.ok(commentService.getCommentCount(contentId));
    }

//    @GetMapping
//    public ResponseEntity<Page<CommentListResponse>> getCommentPageByContentId(
//            @PathVariable Long contentId,
//            @PageableDefault(size = 5, sort = "commentId", direction = Sort.Direction.DESC) Pageable pageable) {
//        return ResponseEntity.ok(commentService.getCommentPageByContentId(contentId, pageable));
//    }

//    @GetMapping
//    public ResponseEntity<Slice<CommentListResponse>> getCommentSliceByContentId(
//            @PathVariable Long contentId,
//            @PageableDefault(size = 5, sort = "commentId", direction = Sort.Direction.DESC) Pageable pageable) {
//        return ResponseEntity.ok(commentService.getCommentSliceByContentId(contentId, pageable));
//    }

    /**
     * 특정 이벤트의 댓글 목록을 Keyset-Filtering 방식으로 조회합니다.
     * @param contentId
     * @param cursor
     * @return
     */
    @GetMapping
    public ResponseEntity<CommentScrollResponse> getCommentScrollByContentId(
            @PathVariable Long contentId,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(commentService.getCommentScrollByContentId(contentId, cursor));
    }

    /**
     * 특정 이벤트에 새 댓글을 등록합니다.
     *
     * @param contentId            이벤트 컨텐츠 ID
     * @param commentInsertRequest 댓글 등록 요청 정보
     * @param customPrincipal      인증된 사용자 정보
     * @return 등록된 댓글 정보
     */
    @PostMapping
    public ResponseEntity<CommentResponse> insertComment(
            @PathVariable Long contentId,
            @Valid @RequestBody CommentInsertRequest commentInsertRequest,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity
                .ok(commentService.insertComment(contentId, commentInsertRequest, customPrincipal));
    }

    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param commentId       댓글 ID
     * @param customPrincipal 인증된 사용자 정보
     * @return 삭제된 댓글 정보
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity.ok(commentService.deleteComment(commentId, customPrincipal));
    }

    /**
     * 특정 댓글을 수정합니다.
     *
     * @param commentId            댓글 ID
     * @param commentUpdateRequest 댓글 수정 요청 정보
     * @param customPrincipal      인증된 사용자 정보
     * @return 수정된 댓글 정보
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return ResponseEntity
                .ok(commentService.updateComment(commentId, commentUpdateRequest, customPrincipal));
    }

}
