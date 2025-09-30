package com.event.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CommentScrollResponse(
        List<CommentResponse> commentResponseList,
        String nextCursor
) {
}
