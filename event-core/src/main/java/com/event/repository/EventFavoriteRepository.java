package com.event.repository;

import com.event.model.entity.EventFavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventFavoriteRepository extends JpaRepository<EventFavoriteEntity, Long> {

    boolean existsByContentIdAndUserId(Long contentId, Long userId);

    void deleteByContentIdAndUserId(Long contentId, Long userId);

}
