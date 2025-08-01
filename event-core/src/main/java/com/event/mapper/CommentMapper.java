package com.event.mapper;

import com.event.model.entity.CommentEntity;
import com.event.model.response.CommentListResponse;
import com.event.model.response.CommentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentListResponse toCommentListResponse(CommentEntity commentEntity);

    CommentResponse toCommentResponse(CommentEntity commentEntity);

}
