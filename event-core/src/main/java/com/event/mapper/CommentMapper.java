package com.event.mapper;

import com.event.model.entity.CommentEntity;
import com.event.model.response.CommentListResponse;
import com.event.model.response.CommentResponse;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentListResponse toCommentListResponse(CommentEntity commentEntity);

    CommentResponse toCommentResponse(CommentEntity commentEntity);

    default LocalDateTime toKst(Instant instant) {
        return (instant == null) ? null : LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

}
