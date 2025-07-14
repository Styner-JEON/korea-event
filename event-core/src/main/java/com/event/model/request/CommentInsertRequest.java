package com.event.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentInsertRequest(
        @NotBlank
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력할 수 있습니다.")
        String content,
        Long contentId
) {
}
