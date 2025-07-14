package com.event.controller;

import com.event.exception.CustomCommentException;
import com.event.model.request.CommentInsertRequest;
import com.event.model.request.CommentUpdateRequest;
import com.event.model.response.CommentListResponse;
import com.event.model.response.CommentResponse;
import com.event.security.CustomPrincipal;
import com.event.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/events/${api.version}/{contentId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class CommentController {

        private final CommentService commentService;

        @PostMapping
        public ResponseEntity<CommentResponse> insertComment(
                        @Valid @RequestBody CommentInsertRequest commentInsertRequest,
                        @AuthenticationPrincipal CustomPrincipal customPrincipal) {
                return ResponseEntity.ok(commentService.insertComment(commentInsertRequest, customPrincipal));
        }

        @GetMapping
        public ResponseEntity<Slice<CommentListResponse>> getCommentsByContentId(
                        @PathVariable Long contentId,
                        @PageableDefault(size = 50,
                                        // page = 0,
                                        sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
                return ResponseEntity.ok(commentService.getCommentsByContentId(contentId, pageable));
        }

        @DeleteMapping("/{commentId}")
        public ResponseEntity<CommentResponse> deleteComment(
                        @PathVariable Long commentId,
                        @AuthenticationPrincipal CustomPrincipal customPrincipal) {
                return ResponseEntity.ok(commentService.deleteComment(commentId, customPrincipal));
        }

        @PutMapping("/{commentId}")
        public ResponseEntity<CommentResponse> updateComment(
                        @PathVariable Long commentId,
                        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest,
                        @AuthenticationPrincipal CustomPrincipal customPrincipal) {
                return ResponseEntity
                                .ok(commentService.updateComment(commentId, commentUpdateRequest, customPrincipal));
        }

}
