package com.event.service;

import com.event.exception.CustomEventException;
import com.event.model.entity.EventFavoriteEntity;
import com.event.model.response.EventFavoriteResponse;
import com.event.repository.EventFavoriteRepository;
import com.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventFavoriteService {

    private final EventRepository eventRepository;

    private final EventFavoriteRepository favoriteRepository;

    /**
     * 즐겨찾기 등록
     * @param contentId
     * @param userId
     * @return
     */
    @Transactional
    public EventFavoriteResponse addFavorite(Long contentId, Long userId) {
        if (!eventRepository.existsById(contentId)) {
            log.debug("Event not found (favorite add) with contentId: {}", contentId);
            throw new CustomEventException(HttpStatus.NOT_FOUND, "Event not found with contentId: " + contentId);
        }

        boolean favoriteStatus = favoriteRepository.existsByContentIdAndUserId(contentId, userId);
        if (!favoriteStatus) {
            favoriteRepository.save(new EventFavoriteEntity(contentId, userId));
        }

        return new EventFavoriteResponse(true, contentId, userId);
    }

    /**
     * 즐겨찾기 해제
     * @param contentId
     * @param userId
     * @return
     */
    @Transactional
    public EventFavoriteResponse removeFavorite(Long contentId, Long userId) {
        boolean favoriteStatus = favoriteRepository.existsByContentIdAndUserId(contentId, userId);
        if (favoriteStatus) {
            favoriteRepository.deleteByContentIdAndUserId(contentId, userId);
        }

        return new EventFavoriteResponse(false, contentId, userId);
    }

    /**
     * 즐겨찾기 여부 확인
     * @param contentId
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(Long contentId, Long userId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByContentIdAndUserId(contentId, userId);
    }

}
