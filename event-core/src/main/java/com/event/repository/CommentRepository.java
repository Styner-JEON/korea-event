package com.event.repository;

import com.event.model.entity.CommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    Slice<CommentEntity> findByContentId(Long contentId, Pageable pageable);

    List<CommentEntity> findAllByContentId(Long contentId);

    int countByContentId(Long contentId);

    List<CommentEntity> findTop10ByContentIdOrderByUpdatedAtDesc(Long contentId);

}
