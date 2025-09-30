package com.event.repository;

import com.event.model.entity.CommentEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    int countByContentId(Long contentId);

    Page<CommentEntity> findPageByContentId(Long contentId, Pageable pageable);

    Slice<CommentEntity> findSliceByContentId(Long contentId, Pageable pageable);

    List<CommentEntity> findAllByContentId(Long contentId);

    List<CommentEntity> findByContentIdOrderByUpdatedAtDesc(Long contentId, Pageable pageable);

    Window<CommentEntity> findScrollByContentId(
            Long contentId,
            Sort sort,
            Limit limit,
            KeysetScrollPosition keysetScrollPosition
    );

}
